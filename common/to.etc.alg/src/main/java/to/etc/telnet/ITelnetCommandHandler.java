/*
 * DomUI Java User Interface - shared code
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
package to.etc.telnet;

import to.etc.util.*;

/**
 *	This interface allows easy extension of the available telnet commands by
 *  allowing a user to add commands to the set. This is typically done by creating
 *  a class inplementing this interface, and then registering the class by
 *  calling LogMaster.registerTelnetCommand.
 *	Usually an anonymous class is used for this, because the actual command
 *  handler is usually a static member of a given class. A typical registration
 *  for such a static thing could be:
 *  <pre>
 *  //-- Add the logmaster command handler.
 *  iTelnetCommandHandler lh = new iTelnetCommandHandler()
 *  {
 *     public boolean executeTelnetCommand(TelnetPrintWriter tpw, CmdStringDecoder commandline) throws Exception
 *     {
 *        return MyClass.executeTelnetCommand(tpw, commandline);
 *     }
 *  };
 *  LogMaster.registerTelnetCommand(lh);
 *  </pre>
 *
 *	<p>
 *  The telnet command handler returns TRUE if it has executed the command and
 *  false if not. For all entered commands ALL handlers are ALWAYS called, so
 *  you must take care NOT to report an error if you do not recognise a command.
 *  If a command is NOT recognised by any registered handler an error message
 *  is output to the issuing session.
 *  A typical implementation of a command handler would be:
 *  <pre>
 *  static protected boolean executeTelnetCommand(TelnetPrintWriter ts, CmdStringDecoder cmd) throws Exception
 *  {
 *		if(cmd.currIs("?"))  // Help command (global)?
 *		{
 *			ts.println(USAGE);
 *			return true;
 *		}
 *		if(! cmd.currIs("myclass")) return false;	// all my commands start with myclass
 *
 *		if(! cmd.hasMore() || cmd.currIs("?"))		// myclass ? is my usage exclusively.
 *		{
 *			ts.println(USAGE);
 *			return true;
 *		}
 *		else if(cmd.currIs("fil*"))					// myclass file?
 *		{
 *			.... handle this command....
 *      }
 *      return false; // Unrecognised!
 *  }
 *  </pre>
 *  </p>
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public interface ITelnetCommandHandler {
	boolean executeTelnetCommand(TelnetPrintWriter tpw, CmdStringDecoder commandline) throws Exception;
}
