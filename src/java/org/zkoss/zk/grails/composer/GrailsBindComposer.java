package org.zkoss.zk.grails.composer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.zkoss.bind.AnnotateBinder;
import org.zkoss.bind.Binder;
import org.zkoss.bind.Converter;
import org.zkoss.bind.Validator;
import org.zkoss.bind.impl.BindEvaluatorXUtil;
import org.zkoss.bind.impl.ValidationMessagesImpl;
import org.zkoss.bind.sys.BindEvaluatorX;
import org.zkoss.bind.sys.BinderCtrl;
import org.zkoss.bind.sys.ValidationMessages;
import org.zkoss.lang.Strings;
import org.zkoss.util.IllegalSyntaxException;
import org.zkoss.zk.grails.BinderAware;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.metainfo.Annotation;
import org.zkoss.zk.ui.metainfo.ComponentInfo;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.sys.ComponentCtrl;
import org.zkoss.zk.ui.util.Composer;
import org.zkoss.zk.ui.util.ComposerExt;

public class GrailsBindComposer<T extends Component> implements Composer<T>, ComposerExt<T>, Serializable, ApplicationContextAware {

    private static final long serialVersionUID = 5858943172681780132L;

    private static final String VM_ID = "$VM_ID$";
    private static final String BINDER_ID = "$BINDER_ID$";

    private Object _viewModel;
    private Binder _binder;
    private final Map<String, Converter> _converters;
    private final Map<String, Validator> _validators;

    @Autowired private ApplicationContext applicationContext;

    private static final String ID_ANNO = "id";
    private static final String INIT_ANNO = "init";

    private static final String COMPOSER_NAME_ATTR = "composerName";
    private static final String VIEW_MODEL_ATTR = "viewModel";
    private static final String BINDER_ATTR = "binder";
    private static final String VALIDATION_MESSAGES_ATTR = "validationMessages";



    public GrailsBindComposer() {
        setViewModel(this);
        _converters = new HashMap<String, Converter>(8);
        _validators = new HashMap<String, Validator>(8);
    }

    public Binder getBinder() {
        return _binder;
    }

    //can assign a separate view model, default to this
    public void setViewModel(Object viewModel) {
        _viewModel = viewModel;
        if (this._binder != null) {
            this._binder.setViewModel(_viewModel);
        }
    }

    public Object getViewModel() {
        return _viewModel;
    }

    public Converter getConverter(String name) {
        Converter conv = _converters.get(name);
        return conv;
    }

    public Validator getValidator(String name) {
        Validator validator = _validators.get(name);
        return validator;
    }

    public void addConverter(String name, Converter converter) {
        _converters.put(name, converter);
    }

    public void addValidator(String name, Validator validator) {
        _validators.put(name, validator);
    }

    //--Composer--//
    @SuppressWarnings("unchecked")
    public void doAfterCompose(T comp) throws Exception {
        BindEvaluatorX evalx = BindEvaluatorXUtil.createEvaluator(null);

        //name of this composer
        String cname = (String)comp.getAttribute(COMPOSER_NAME_ATTR);
        comp.setAttribute(cname != null ? cname : comp.getId()+"$composer", this);

        //init viewmodel first
        _viewModel = initViewModel(evalx, comp);
        _binder = initBinder(evalx, comp);
        if(_viewModel instanceof BinderAware) {
            ((BinderAware)_viewModel).setBinder(_binder);
        }
        ValidationMessages _vmsgs = initValidationMessages(evalx, comp, _binder);

        //wire before call init
        Selectors.wireComponents(comp, _viewModel, true);
        Selectors.wireVariables(comp, _viewModel, Selectors.newVariableResolvers(_viewModel.getClass(), null));
        if(_vmsgs!=null){
            ((BinderCtrl)_binder).setValidationMessages(_vmsgs);
        }
        //init
        _binder.init(comp, _viewModel);
        //load data
        _binder.loadComponent(comp,true); //load all bindings
    }

    @SuppressWarnings("unchecked")
    private Object initViewModel(BindEvaluatorX evalx, Component comp) {
        final ComponentCtrl compCtrl = (ComponentCtrl) comp;
        final Annotation idanno = compCtrl.getAnnotation(VIEW_MODEL_ATTR, ID_ANNO);
        final Annotation initanno = compCtrl.getAnnotation(VIEW_MODEL_ATTR, INIT_ANNO);
        String vmname = null;
        Object vm = null;

        if(idanno==null && initanno==null){
            return _viewModel;
        }else if(idanno==null){
            throw new IllegalSyntaxException("you have to use @id to assign the name of view model for "+comp);
        }else if(initanno==null){
            throw new IllegalSyntaxException("you have to use @init to assign the view model for "+comp);
        }

        vmname = BindEvaluatorXUtil.eval(evalx,comp,idanno.getAttribute("value"),String.class);
        vm = BindEvaluatorXUtil.eval(evalx,comp,initanno.getAttribute("value"),Object.class);

        if(Strings.isEmpty(vmname)){
            throw new UiException("name of view model is empty");
        }

        try {
            if(vm instanceof String){
                String beanOrClassName = (String)vm;
                if(applicationContext.containsBean(beanOrClassName)) {
                    vm = applicationContext.getBean(beanOrClassName);
                } else {
                    Class<?> vmClass = comp.getPage().resolveClass(beanOrClassName);
                    try {
                        vm = applicationContext.getBean(vmClass);
                    } catch(BeansException be) {
                        vm = vmClass;
                    }
                }
            }
            if(vm instanceof Class<?>){
                vm = ((Class<?>)vm).newInstance();
            }
        } catch (Exception e) {
            throw new UiException(e.getMessage(),e);
        }
        if(vm == null){
            throw new UiException("view model of '"+vmname+"' is null");
        }else if(vm.getClass().isPrimitive()){
            throw new UiException("view model '"+vmname+"' is a primitive type, is "+vm);
        }
        comp.setAttribute(vmname, vm);
        comp.setAttribute(VM_ID, vmname);

        return vm;
    }

    private Binder initBinder(BindEvaluatorX evalx, Component comp) {
        final ComponentCtrl compCtrl = (ComponentCtrl) comp;
        final Annotation idanno = compCtrl.getAnnotation(BINDER_ATTR, ID_ANNO);
        final Annotation initanno = compCtrl.getAnnotation(BINDER_ATTR, INIT_ANNO);
        Object binder = null;
        String bname = null;

        if(idanno!=null){
            bname = BindEvaluatorXUtil.eval(evalx,comp,idanno.getAttribute("value"),String.class);
        }else{
            bname = "binder";
        }
        if(Strings.isEmpty(bname)){
            throw new UiException("name of binder is empty");
        }

        if(initanno!=null){
            binder = BindEvaluatorXUtil.eval(evalx,comp,initanno.getAttribute("value"),Object.class);
            try {
                if(binder instanceof String){
                    binder = comp.getPage().resolveClass((String)binder);
                }
                if(binder instanceof Class<?>){
                    binder = ((Class<?>)binder).newInstance();
                }
            } catch (Exception e) {
                throw new UiException(e.getMessage(),e);
            }
            if(!(binder instanceof Binder)){
                throw new UiException("evaluated binder is not a binder is "+binder);
            }
        }else{
            binder = new AnnotateBinder();
        }

        //put to attribute, so binder could be referred by the name
        comp.setAttribute(bname, binder);
        comp.setAttribute(BINDER_ID, bname);

        return (Binder)binder;
    }

    private ValidationMessages initValidationMessages(BindEvaluatorX evalx, Component comp,Binder binder) {
        final ComponentCtrl compCtrl = (ComponentCtrl) comp;
        final Annotation idanno = compCtrl.getAnnotation(VALIDATION_MESSAGES_ATTR, ID_ANNO);
        final Annotation initanno = compCtrl.getAnnotation(VALIDATION_MESSAGES_ATTR, INIT_ANNO);
        Object vmessages = null;
        String vname = null;

        if(idanno!=null){
            vname = BindEvaluatorXUtil.eval(evalx,comp,idanno.getAttribute("value"),String.class);
        }else{
            return null;//validation messages is default null
        }
        if(Strings.isEmpty(vname)){
            throw new UiException("name of ValidationMessages is empty");
        }

        if(initanno!=null){
            vmessages = BindEvaluatorXUtil.eval(evalx,comp,initanno.getAttribute("value"),Object.class);
            try {
                if(vmessages instanceof String){
                    vmessages = comp.getPage().resolveClass((String)vmessages);
                }
                if(vmessages instanceof Class<?>){
                    vmessages = ((Class<?>)vmessages).newInstance();
                }
            } catch (Exception e) {
                throw new UiException(e.getMessage(),e);
            }
            if(!(vmessages instanceof ValidationMessages)){
                throw new UiException("evaluated validationMessages is not a ValidationMessages is "+vmessages);
            }
        }else{
            vmessages = new ValidationMessagesImpl();
        }

        //put to attribute, so binder could be referred by the name
        comp.setAttribute(vname, vmessages);

        return (ValidationMessages)vmessages;
    }


    //--ComposerExt//
    public ComponentInfo doBeforeCompose(Page page, Component parent,
            ComponentInfo compInfo) throws Exception {
        return compInfo;
    }

    public void doBeforeComposeChildren(Component comp) throws Exception {
    }

    public boolean doCatch(Throwable ex) throws Exception {
        return false;
    }

    public void doFinally() throws Exception {
        // ignore
    }

    //--notifyChange--//
    public void notifyChange(Object bean, String property) {
        getBinder().notifyChange(bean, property);
    }

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        this.applicationContext = ctx;
    }

}