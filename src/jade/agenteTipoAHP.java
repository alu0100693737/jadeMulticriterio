package jade;
import jade.core.Agent;

public class agenteTipoAHP extends Agent {
	
	protected void setup() { 
		System.out.println("Creando el agente");
		addBehaviour(new comportamientoElectre(this));
	}
	
	protected void takeDown() { 
		System.out.println("Eliminando el agente");
	}
}
