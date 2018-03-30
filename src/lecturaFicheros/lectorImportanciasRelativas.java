package lecturaFicheros;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * Clase para la lectura de las importancias relativas / pesos de todos los usuarios
 * @author Ivan Garcia Campos   alu0100693737@ull.edu.es
 * @version 1.0, 29/03/2018
 * Asignatura "Sistemas Inteligentes Avanzados"
 * Master en Ingeniería Informática por la ULL
 */
public class lectorImportanciasRelativas {
	
	/**
	 * ArrayList de tipo importanciaRelativaIndividual que almacena todas las importancias relativas de los usuarios
	 */
	private ArrayList<importanciaRelativaIndividual> importanciasRelativas;
	
	/**
	 * Constructor de la clase lectorImportanciasRelativas
	 * @param archivo 			Archivo a leer, extraccion de datos
	 * @param numCriterios		Numero de criterios/atributos del problema. Comprobación fichero correcto
	 */
	public lectorImportanciasRelativas(String archivo, int numCriterios)  throws Exception {
		importanciasRelativas = new ArrayList<importanciaRelativaIndividual>();
		
		leerFicheroImportancias(archivo, numCriterios);
		showImportanciasRelativas();
	}
	
	/**
	 * Metodo para la lectura de las importancias relativas de los usuarios
	 * @param archivo 			Archivo a leer, extraccion de datos
	 * @param numCriterios		Numero de criterios/atributos del problema. Comprobación fichero correcto
	 */
	public void leerFicheroImportancias(String archivo, int numCriterios)  throws Exception {
		String cadena;
		BufferedReader b = null;
		FileReader f = null;

		try {
			f = new FileReader(archivo);
			try {
			b = new BufferedReader(f);
			while((cadena = b.readLine()) != null) {
				importanciaRelativaIndividual aux = new importanciaRelativaIndividual(cadena, numCriterios);
				getImportanciasRelativas().add(aux);
			}
			} catch(Exception E) {
				System.err.println("No se ha podido leer el fichero");
			} finally {
				if(b != null)
					b.close();
			}
		} catch(Exception E) {
			System.err.println("No se ha podido leer el fichero");
		} finally {
			if(b != null)
				b.close();
			if(f != null) 
				f.close();
		}
	}
	
	/**
	 * Metodo que muestra todas las importancias relativas de los usuarios
	 */
	public void showImportanciasRelativas() {
		System.out.println("\nImportancias Relativas");
		if(getImportanciasRelativas().size() > 0) {
			for(int i = 0; i < getImportanciasRelativas().size(); i ++) {
				getImportanciasRelativas().get(i).showImportancias();
			}
		}
	}
	
	/************************ GETS y SETS ************************/
	
	/**
	 * Metodo que devuelve el conjunto de importancias relativas
	 * @return ArrayList<importanciaRelativaIndividual>
	 */
	public ArrayList<importanciaRelativaIndividual> getImportanciasRelativas() {
		return importanciasRelativas;
	}
}
