package jade;
import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;

public class PrimerAgente extends Agent {
	
	private String titulo;
	
	protected void setup() { 
		System.out.println("Creando el agente");
		addBehaviour(new myBehaviour(this));
	}
	
	protected void takeDown() { 
		System.out.println("Eliminando el agente");
	}
} 

