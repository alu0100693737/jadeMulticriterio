package jade;
import jade.core.Agent;

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

