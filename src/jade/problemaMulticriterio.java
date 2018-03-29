package jade;

import java.io.FileNotFoundException;
import java.io.IOException;

public class problemaMulticriterio {
	
	private leerProblema lectorFichero_;
	
	public problemaMulticriterio(String archivo) throws IllegalArgumentException, Exception {
		lectorFichero_ = new leerProblema(archivo);
	}
}
