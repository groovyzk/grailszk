/* ViewModelArtefactHandler.java

Copyright (C) 2008, 2009 Chanwit Kaewkasi

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

package org.zkoss.zk.grails.artefacts;



import grails.core.ArtefactHandlerAdapter;
import org.grails.core.artefact.DomainClassArtefactHandler;

/**
 *
 * @author Chanwit Kaewkasi (chanwit@gmail.com)
 *
*/
public class ViewModelArtefactHandler extends ArtefactHandlerAdapter {

    public static final String TYPE = "ViewModel";

    public ViewModelArtefactHandler() {
        super(TYPE, GrailsViewModelClass.class,
            DefaultGrailsViewModelClass.class,
            DefaultGrailsViewModelClass.VIEW_MODEL,
            false);
    }

    public boolean isArtefactClass(@SuppressWarnings("rawtypes") Class clazz) {
        return super.isArtefactClass(clazz) &&
               !DomainClassArtefactHandler.isDomainClass(clazz);
    }
}
