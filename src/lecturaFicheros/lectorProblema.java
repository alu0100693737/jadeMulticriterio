package lecturaFicheros;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * Clase lectorProblema
 * Lee un archivo con la información del problema multicriterio y la almacena en 
 * una estructura valida.
 * @author Ivan Garcia Campos   alu0100693737@ull.edu.es
 * @version 1.0, 29/03/2018
 * Asignatura "Sistemas Inteligentes Avanzados"
 * Master en Ingeniería Informática por la ULL
 * 
 * Ej:
 * 
 * 4 6 1 													-> alternativas, atributos 1 si tiene descripcion, 0 datos directos 
 * velMax distDespegue cargaMax precio fiabilidad manejo	-> descripcion, puede estar o no
 * 1 0 1 0 1 1 												-> segunda linea max(1) o min(0)
 * 2.0 1.5 2.0 5.5 5 9										-> datos
 * 2.5 2.7 1.8 6.5 3 5
 * 1.8 2.0 2.1 4.5 7 7
 * 2.2 1.8 2.0 5.0 5 5
 */
public class lectorProblema {

	/**
	 * Numero de alternativas para el decisor
	 */
	private int numAlternativas_;
	/**
	 * Numero de criterios a tener en cuenta
	 */
	private int numCriterios_;
	/**
	 * Array con los nombres de los criterios, parametro no obligatorio
	 */
	private ArrayList<String> nombreAtributos_;
	/**
	 * Array que indica que criterio debe maximizarse y cual minimizarse (1 para max, 0 para min)
	 */
	private ArrayList<Boolean> maxminAtributos_;
	/**
	 * Array con los valores de todos los criterios para todas las alternativas 
	 */
	private ArrayList<ArrayList<Float>> valoresAtributos_;

	/**
	 * Constructor de la clase, lee el fichero y lo muestra
	 * @param archivo		Archivo a analizar
	 * @throws IllegalArgumentException, Exception si algun dato del fichero es incorrecto, sobra o falta elemento.
	 */
	public lectorProblema(String archivo) throws IllegalArgumentException, Exception {
		numAlternativas_ = 0;
		numCriterios_ = 0;
		maxminAtributos_ = new ArrayList<Boolean>();
		valoresAtributos_ = new ArrayList<ArrayList<Float>>();

		leerFichero(archivo);
		showFichero();
	}

	/**
	 * Método que lee el fichero especificado por parametro
	 * @param archivo		Archivo a analizar
	 * @throws IllegalArgumentException, Exception si algun dato del fichero es incorrecto, sobra o falta elemento.
	 */
	private void leerFichero(String archivo)  throws IllegalArgumentException, Exception {
		String cadena;
		BufferedReader b = null;

		try {
			FileReader f = new FileReader(archivo);
			b = new BufferedReader(f);
			cadena = b.readLine();

			//Primera linea -> alternativas, atributos y si existe nombre de atributos 
			String[] splited = cadena.split("\\s+");
			if(splited.length == 3) {

				setNumAlternativas(Integer.parseInt(splited[0]));
				setNumCriterios(Integer.parseInt(splited[1]));

				//Existe nombre para los atributos del problema
				if(Integer.parseInt(splited[2]) == 1) {
					nombreAtributos_ = new ArrayList<String>();

					cadena = b.readLine();
					splited = cadena.split("\\s+");
					if(splited.length == getNumCriterios()) 
						for(int i = 0; i < getNumCriterios(); i++) 
							getArrayNombreAtributos().add(splited[i]);
					else {
						System.err.println("Error en el fichero, la linea de atributos debe tener el mismo lenght que se indica en la primera linea");
						b.close();
						return;
					}	
				}
			} else {
				System.err.println("Error en el fichero, la primera linea debe contener: \n\tNumAlternativas. \n\tNumAtributos. \n\t");
				b.close();
				return;
			}

			//Atributos max o min
			cadena = b.readLine();
			splited = cadena.split("\\s+");
			if(splited.length == getNumCriterios()) 
				for(int i = 0; i < getNumCriterios(); i++) 
					getArrayMaxMinAtributos().add(intToBool(Integer.parseInt(splited[i])));
	
			else {
				System.err.println("Error en el fichero, la linea de atributos debe tener el mismo lenght que se indica en la primera linea");
				b.close();
				return;
			}	

			//Valor de los atributos para cada alternativa
			while((cadena = b.readLine()) != null) {
				splited = cadena.split("\\s+");
				ArrayList<Float> array = new ArrayList<Float>();
				if(splited.length == getNumCriterios()) {
					for(int i = 0; i < splited.length; i++)
						array.add(Float.parseFloat(splited[i]));
					getArrayValoresAtributos().add(array);
				} else { 
					System.err.println("Error leyendo los atributos del fichero");
					b.close();
					return;
				}
			}
		} catch(Exception E) {
			System.err.println("No se ha podido leer el fichero");
		} finally {
			if(b != null)
				b.close();
		}
	}

	/**
	 * Metodo que muestra el fichero del problema multicriterio
	 */
	public void showFichero() {
		if(getNumAlternativas() == 0) {
			System.out.println("Fichero aun no leido, esta condicion no deberia ocurrir");
		} else {
			System.out.println("Fichero de entrada: ");
			System.out.print(getNumAlternativas() + " " + getNumCriterios());
			if(getArrayNombreAtributos().size() > 0) {
				System.out.println(" 1");
				for(int i = 0; i < getArrayNombreAtributos().size(); i++) {
					System.out.print(getArrayNombreAtributos().get(i) + " ");
				}
			}
			System.out.println();

			for(int i = 0; i < getArrayMaxMinAtributos().size(); i++) {
				System.out.print(getArrayMaxMinAtributos().get(i) + " ");
			}
			System.out.println();

			for(int i = 0; i < getArrayValoresAtributos().size(); i++) {
				for(int j = 0; j < getNumCriterios(); j++) 
					System.out.print(getArrayValoresAtributos().get(i).get(j) + " ");
				System.out.println();
			}
		}
	}

	/**
	 * Metodo que cambia un entero 0 o 1 en false y true, tipo C++
	 * @return static boolean
	 */
	public static boolean intToBool(int input) throws Exception {
		if (input < 0 || input > 1) {
			throw new Exception("input must be 0 or 1");
		}

		// Note we designate 1 as true and 0 as false though some may disagree
		return input == 1;
	}

	/**
	 * Metodo que devuelve el numero de alternativas del problema
	 * @return int
	 */
	public int getNumAlternativas() {
		return numAlternativas_;
	}

	/**
	 * Metodo que asigna el valor del numero de alternativas
	 * @param valor
	 */
	public void setNumAlternativas(int valor) {
		numAlternativas_ = valor;
	}

	/**
	 * Metodo que devuelve el numero de criterios del problema
	 * @return int
	 */
	public int getNumCriterios() {
		return numCriterios_;
	}

	/**
	 * Metodo que asigna el valor del numero de criterios del problema
	 */
	public void setNumCriterios(int valor) {
		numCriterios_ = valor;
	}

	/**
	 * Metodo que devuelve el array con los nombres de los atributos/criterios a medir
	 * @return ArrayList<String>
	 */
	public ArrayList<String> getArrayNombreAtributos() {
		return nombreAtributos_;
	}

	/**
	 * Metodo que devuelve el array que indica que criterio debe maximizarse o minimizarse
	 * @return ArrayList<Boolean>
	 */
	public ArrayList<Boolean> getArrayMaxMinAtributos() {
		return maxminAtributos_;
	}

	/**
	 * Metodo que devuelve el array con los valores de cada uno de los atributos
	 * @return ArrayList<ArrayList<Float>
	 */
	public ArrayList<ArrayList<Float>> getArrayValoresAtributos() {
		return valoresAtributos_;
	}

	/**
	 * Metodo que añade los valores de un determinado criterio/atributo al array de atributos/criterios
	 * @param ArrayList<Float> 
	 */
	public void setArrayValoresAtributo(ArrayList<Float> valores) {
		getArrayValoresAtributos().add(valores);
	}
}

