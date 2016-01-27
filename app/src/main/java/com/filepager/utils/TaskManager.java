package com.filepager.utils;

import java.util.LinkedList;
import java.util.NoSuchElementException;

public class TaskManager {
	
private LinkedList<Runnable> tasks;
private boolean running;
private Runnable internal;
private Thread[] worker;

public TaskManager()
{
	running=false;
	tasks=new LinkedList<Runnable>();
	internal = new Runnable() {
		
		@Override
		public void run() {
			while(running)
			{
				try
				{
				Runnable task = getNextTask();
				task.run();
				}
				catch(NoSuchElementException e)
				{
					e.printStackTrace();
				}
				catch(Throwable t)
				{
					t.printStackTrace();					
				}
			}
		}
	};
}
public void start(){
	if(!running)
	{
		running=true;
		worker = new Thread[2];
		
		worker[0] = new Thread(internal);
		worker[0].start();

		worker[1] = new Thread(internal);
		worker[1].start();
	}
}
public void stop()
{
	running=false;
}
public void addTask(Runnable runthis)
{
	synchronized (tasks) {
		tasks.add(runthis);
		tasks.notify();
	}
	
}
public void addPriorityTask(Runnable runthis)
{
	synchronized (tasks) {
		tasks.add(0, runthis);
		tasks.notify();
	}
	
}
private Runnable getNextTask()
{
	synchronized (tasks) {
		if(tasks.isEmpty())
		{
			try
			{
				tasks.wait();
			}
			
			catch (InterruptedException e)
			{
				
			}
		}
		return tasks.removeFirst();
	}
}
}
