package jade;

import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;

public class myBehaviour extends SimpleBehaviour {

	private int iteration_;
	
	public myBehaviour(Agent a) {
		super(a);
	}
	
	@Override
	public void action() {
		if(getIteration() < 1) {
			System.out.println("¿Ejecutando el comportamiento?");
			addOneIteration();
		} else {
			done();
		}
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return false;
	}
	
	public int getIteration() {
		return iteration_;
	}
	
	public void addOneIteration() {
		iteration_++;
	}

}

