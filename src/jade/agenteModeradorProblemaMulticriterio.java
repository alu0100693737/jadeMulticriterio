package jade;

import jade.core.Agent;
import lecturaFicheros.lectorProblema;
import lecturaFicheros.lectorImportanciasRelativas;

/**
 * Clase que resuelve problemas multicriterios, agente moderador
 *  * @author Ivan Garcia Campos   alu0100693737@ull.edu.es
 * @version 1.0, 29/03/2018
 * Asignatura "Sistemas Inteligentes Avanzados"
 * Master en Ingeniería Informática por la ULL
 */
public class agenteModeradorProblemaMulticriterio extends Agent {
	
	/**
	 * Atributo de la clase leerProblema para la lectura del problema
	 */
	private lectorProblema lectorFichero;
	/**
	 * Atributo de la clase leerImportanciasRelativas para la lectura de importancias por usuarios
	 */
	private lectorImportanciasRelativas lectorFicheroImportancias_;
	
	/**
	 * Variable final que almacena el nombre del fichero que contiene los datos del problema
	 */
	public static final String FICHERO_PROBLEMA = "datosProblema.txt";
	/**
	 * Variable final que almacena el nombre del fichero que contiene las importancias relativas de los usuarios
	 */
	public static final String FICHERO_IMPORTANCIAS_RELATIVAS = "importanciaRelativaPersonal.txt";
	
	/**
	 * Metodo para la inicializacion del agente moderador
	 */
	protected void setup() {     
		try {
			lectorFichero = new lectorProblema(FICHERO_PROBLEMA);
			lectorFicheroImportancias_ = new lectorImportanciasRelativas(FICHERO_IMPORTANCIAS_RELATIVAS, getLectorFichero().getNumCriterios());
		} catch (Exception e) {
			System.err.println("Error en el agente moderador");
			return;			
		}
		System.out.println("Agente moderador, ya se han leido los ficheros");
		
		System.out.println("PEPE");
	}
	
	/**
	 * Metodo Override, Agent para la finalizacion del agente
	 */
	protected void takeDown() { 
		System.out.println("Eliminando el agente");
	}
	
	/**
	 * Metodo que devuelve el atributo de la clase leerProblema, lectorFichero_
	 * @return lectorFichnero_
	 */
	public lectorProblema getLectorFichero() {
		return lectorFichero;
	}
	
	/**
	 * Metodo que devuelve el atributo de la clase leerImportanciasRelativas, lectorFicheroImportancias_
	 * @return lectorFicheroImportancias_
	 */
	public lectorImportanciasRelativas getLectorFicheroImportancias() {
		return lectorFicheroImportancias_;
	}
}
