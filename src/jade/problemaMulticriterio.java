package jade;

/*
 * Clase que resuelve problemas multicriterios
 */
public class problemaMulticriterio {
	
	/**
	 * Atributo de la clase leerProblema para la lectura del problema
	 */
	private leerProblema lectorFichero_;
	
	/*
	 * Constructor de la clase problemaMulticriterio
	 */
	public problemaMulticriterio(String archivo) throws IllegalArgumentException, Exception {
		lectorFichero_ = new leerProblema(archivo);
	}
}
