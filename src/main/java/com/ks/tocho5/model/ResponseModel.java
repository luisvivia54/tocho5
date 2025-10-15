package com.ks.tocho5.model;

import java.util.List;

public class ResponseModel<T> {

	private String exit_status;
	private String err_description;
	private Object datos;

	public String getExit_status() {
		return exit_status;
	}

	public void setExit_status(String exit_status) {
		this.exit_status = exit_status;
	}

	public String getErr_description() {
		return err_description;
	}

	public void setErr_description(String err_description) {
		this.err_description = err_description;
	}

	public Object getDato() {
		return datos;
	}

	public void setDato(Object datos) {
		this.datos = datos;
	}

	public void setDatos(List<?> datos) {
		this.datos = datos;
	}

	@Override  //TODO ESTO ES PARA QUE NO NOS REGRESE DATOS DE MEMORIA
	public String toString() {
		return "ResponseModel [exit_status=" + exit_status + ", err_description=" + err_description + ", datos=" + datos + "]";
	}
 
}