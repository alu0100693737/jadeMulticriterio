package jade;

import jade.core.Agent;
import lecturaFicheros.leerProblema;
import lecturaFicheros.leerImportanciasRelativas;

/*
 * Clase que resuelve problemas multicriterios, agente moderador
 */
public class problemaMulticriterioAgenteModerador extends Agent {
	
	/**
	 * Atributo de la clase leerProblema para la lectura del problema
	 */
	private leerProblema lectorFichero_;
	private leerImportanciasRelativas lectorFicheroImportancias_;
	
	public final static String FICHERO_PROBLEMA = "datosProblema.txt";
	public final static String FICHERO_IMPORTANCIAS_RELATIVAS = "importanciaRelativaPersonal.txt";
	
	protected void setup() {     
		try {
			lectorFichero_ = new leerProblema(FICHERO_PROBLEMA);
			lectorFicheroImportancias_ = new leerImportanciasRelativas(FICHERO_IMPORTANCIAS_RELATIVAS);
			
		} catch (Exception e) {
			System.out.println("Error en el agente moderador");
			e.printStackTrace();
		}
		System.out.println("Agente moderador, ya se han leido los ficheros");
	}
	
	protected void takeDown() { 
		System.out.println("Eliminando el agente");
	}

}
