package jade;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import lecturaFicheros.importanciaRelativaIndividual;
import lecturaFicheros.lectorProblema;

public class agenteTipoAHP extends Agent {
	
	private lectorProblema datosProblema;
	private importanciaRelativaIndividual importanciaRelativa;

	protected void setup() { 
		System.out.println("Creando el agente");
		System.out.println("\nHola! El agente "+ getAID().getName()+" está listo.\n");

		Object[] args = getArguments();         // Obtiene los argumentos dados en la inicialización del comprador
		if (args != null && args.length == 2) {  // Tiene que haber al menos un argumento
			if((args[0] instanceof lectorProblema) && (args[1] instanceof importanciaRelativaIndividual)) {	

				datosProblema = new lectorProblema((lectorProblema) args[0]);
				importanciaRelativa = new importanciaRelativaIndividual((importanciaRelativaIndividual) args[1]);
				System.out.println("\n------------------------------------------------------------------\n");
				
				addBehaviour(new CyclicBehaviour() {

					@Override
					public void action() {

						ACLMessage msg = receive();
						if(msg != null) {
							System.out.println(msg.getContent());
						} else
							block();
					}
				});
				addBehaviour(new comportamientoAHP());

			} else {
				System.err.println("Tipos de datos de argumentos erroneo, ERROR");
				return;
			}
		} else {
			System.err.println("Recuerde que debe añadirse al agente el conjunto de datos y su importancia relativa personal, ERROR");
			return;
		}
	}
	class comportamientoAHP extends OneShotBehaviour {

		@Override
		public void action() {
			System.out.println("Comportamiento Proceso Analitico Jerarquico");
		
		}
	}
	
	protected void takeDown() { 
		System.out.println("Eliminando el agente");
	}
}
