package jade;

import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;

public class myBehaviour extends SimpleBehaviour {

	public myBehaviour(Agent a) {
		super(a);
	}
	
	@Override
	public void action() {
		System.out.println("¿Ejecutando el comportamiento?");
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return false;
	}

}

