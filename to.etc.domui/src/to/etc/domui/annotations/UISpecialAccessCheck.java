/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.domui.annotations;

import java.lang.annotation.*;

import to.etc.domui.util.*;

/**
 * Defines the special method that can be used to check if logged user has special access rights to page based on relation to data context.
 * It is used to override required rights for page access when user that tries to access page is owner of data or in some other special relation when general rights are not needed.
 * 
 * This annotation must be used in combination with {@link UIRights}, where {@link UIRights#specialCheckMethod()} must contain name of method that is annotated with UISpecialAccessCheck.
 * 
 * Method annotated by this annotation must be static method that returns boolean with single parameter of type that is matching with id value of page parameter denoted by dataParam annotation attribute.    
 * 
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 15 Dec 2011
 */
@Target(value = ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UISpecialAccessCheck {
	/**
	 * Identifies param that contains marshaled PK of data object that is used for special access check.  
	 * @return
	 */
	String dataParam() default Constants.NONE;
}
