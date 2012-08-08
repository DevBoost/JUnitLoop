package de.devboost.eclipse.jloop.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import de.devboost.eclipse.jloop.JLoopPlugin;

public class StopClassInLoopCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		JLoopPlugin.getDefault().setLoopFile(null);
		return null;
	}
}
