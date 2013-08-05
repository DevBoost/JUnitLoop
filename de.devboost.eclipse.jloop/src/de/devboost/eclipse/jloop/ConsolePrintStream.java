package de.devboost.eclipse.jloop;

import java.io.OutputStream;
import java.io.PrintStream;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * The {@link ConsolePrintStream} is used to redirect output from a class that
 * runs in a loop in the same VM to the Eclipse Console View.
 */
class ConsolePrintStream extends PrintStream {

	private static final String CONSOLE_NAME = ConsolePrintStream.class.getPackage().getName();

	public ConsolePrintStream() {
		super(getOut());
	}

	private static OutputStream getOut() {
		MessageConsole myConsole = findConsole(CONSOLE_NAME);
		MessageConsoleStream out = myConsole.newMessageStream();
		return out;
	}

	private static MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager manager = plugin.getConsoleManager();
		IConsole[] existingConsoles = manager.getConsoles();
		for (int i = 0; i < existingConsoles.length; i++) {
			if (name.equals(existingConsoles[i].getName())) {
				return (MessageConsole) existingConsoles[i];
			}
		}
		
		// no console found, so create a new one
		MessageConsole newConsole = new MessageConsole(name, null);
		manager.addConsoles(new IConsole[] { newConsole });
		return newConsole;
	}
}
