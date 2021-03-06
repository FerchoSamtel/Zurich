package model;

import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import com.mysql.cj.CoreSession;

import controller.Destinatario;
import controller.SendMail;
import model.ConexionBD;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class QuerysBD extends ConexionBD {
	static Destinatario correoDestinatario;
	public String correo;
	public static String correoUser;

	// hacer que el metodo retorne un bool
	@SuppressWarnings("unused")
	static public boolean dataCompleta(String completo, String nomArchivo) throws Throwable {
		String error;
		String[] separacionDatos;
		// re crea un vector el cual se llena con la separación del String que traemos
		// de la Clase (ReadFile)
		separacionDatos = completo.split("\n");
		Date date = new Date();
		// Se instancian varios formatos de fecha el cual se utilizaran mas adelante
		DateFormat hourdateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		// instanciamos el preparedStatement ya que contienen una sentencia SQL que ya
		// ha sido compilada
		PreparedStatement preparedStatement = null;
		// instanciamos la clase Connection la cual representa la conexión con la Base
		// de Datos
		Connection conexion;

		ConexionBD miConexion = new ConexionBD();
		// asignamos la conexión a nuesta BD
		conexion = miConexion.getConnection();
		try {
			// A nuestra conexión le asignamos el AutoCommit en false, para poder utilizar
			// el commit como el rollback, depende de el proceso
			conexion.setAutoCommit(false);

			// se instancian variables que se usan mas adelante para unos inserts
			int idClient = 0;
			int consecutivo = 0;
			int idFuncionario = 0;
			int idRadicacion = 0;
			int clienteIdentCompleta = 0;
			int temp = 0;
			// Se instancia el ResultSet, ya que trae el resultado de algunas consultas
			ResultSet rs;

			// Se crea un ciclo for el cual va rrecorrer todas las lineas una a una que
			// tiene nuestro .txt
			for (int i = 0; i < separacionDatos.length; i++) {
				String[] separacionDatosInsert;
				separacionDatosInsert = separacionDatos[i].split("\\|");

				if (temp == 0) {
					correoDestinatario = new Destinatario();
					correoDestinatario.setCorreo(separacionDatosInsert[10]);
					temp = 1;
				}

				try {

					if (separacionDatosInsert[0].equals("R1")) {
						clienteIdentCompleta = 0;
						idClient = 0;
						consecutivo = 0;
						idFuncionario = 0;
						idRadicacion = 0;

						/*
						 * for (int j = 0; j < separacionDatosInsert.length; j++) { System.out.println(j
						 * +" dato " +separacionDatosInsert[j]); }
						 */
						String sqlInsertClientes = "INSERT INTO `asistemyca_zurich`.`clientes` (`tipo_documento`, `documento`, `created`)"
								+ "VALUES (?, ?,'" + hourdateFormat.format(date) + "')";
						preparedStatement = conexion.prepareStatement(sqlInsertClientes,
								preparedStatement.RETURN_GENERATED_KEYS); // for insert

						// set values
						preparedStatement.setInt(1, Integer.parseInt(separacionDatosInsert[1])); // tipo_documento
						preparedStatement.setString(2, separacionDatosInsert[2]); // documento

						preparedStatement.executeUpdate();

						rs = preparedStatement.getGeneratedKeys();
						if (rs.next()){
							idClient =rs.getInt(1);
							System.out.println("ID Autogenerado:  " + idClient);
						}

						preparedStatement = conexion.prepareStatement("SELECT max(consecutivo)+1 FROM asistemyca_zurich.zr_radicacion");
						preparedStatement.executeQuery();
						rs = preparedStatement.getResultSet();
						if (rs.next()){
							consecutivo = rs.getInt(1);
							System.out.println("consecutivo Autogenerado:  " + consecutivo);

						}
 
						preparedStatement = conexion
								.prepareStatement("SELECT id FROM asistemyca_zurich.users where correo = '"
										+ separacionDatosInsert[10] + "'");
						preparedStatement.executeQuery();
						rs = preparedStatement.getResultSet();
						if (rs.next()) {
							idFuncionario = rs.getInt(1);
							System.out.println("idFuncionario:  " + idFuncionario);
						}
							
						String sqlZrRadicacion = "INSERT INTO `asistemyca_zurich`.`zr_radicacion` ("
								+ "`funcionario_id`, `cliente_id`, `consecutivo`,`numero_planilla`, `tipo_cliente`, "
								+ "`tipo_medio`, `devuelto`, `separado`, `digitalizado`, `cantidad_separada`, `formulario`,"
								+ " `cantidad_documentos`, `medio_recepcion` , `radicacion_proceso`,  `correo_radicacion`,"
								+ "`linea_negocio_id`, `formulario_sarlaft` , `formulario_repetido`, `fecha_diligenciamiento`, `created`)"

								+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,"
								+ "'" + dateFormat.format(date) + "', '" + hourdateFormat.format(date) + "')";

						preparedStatement = conexion.prepareStatement(sqlZrRadicacion.toString(),
								preparedStatement.RETURN_GENERATED_KEYS); // for insert

						if(separacionDatosInsert[5].equalsIgnoreCase("")) {
							separacionDatosInsert[5] = "0";
							System.out.println("cantidad_separada:  " + separacionDatosInsert[5]);
						}
						if(separacionDatosInsert[9].equalsIgnoreCase("")) {
							separacionDatosInsert[9] = "NULL";
							System.out.println("linea_negocio_id:  " + separacionDatosInsert[9]);
						}
						if(separacionDatosInsert[11].equalsIgnoreCase("")) {
							separacionDatosInsert[11] = "1";
							System.out.println("linea_negocio_id:  " + separacionDatosInsert[11]);
						}
						
						// set values
						preparedStatement.setInt(1, idFuncionario); // funcionario_id
						preparedStatement.setInt(2, idClient); // cliente_id
						preparedStatement.setInt(3, consecutivo); // consecutivo
						preparedStatement.setString(4, "prueba"); // numero_planilla
						preparedStatement.setString(5, "Cliente");// tipo_cliente
						preparedStatement.setString(6, separacionDatosInsert[3]); // tipo_medio
						preparedStatement.setString(7, "NO"); // devuelto
						preparedStatement.setString(8, separacionDatosInsert[4]); // separado
						preparedStatement.setString(9, "NO"); // digitalizado
						preparedStatement.setInt(10, Integer.parseInt(separacionDatosInsert[5])); // cantidad_separada
						preparedStatement.setString(11, separacionDatosInsert[6]); // formulario
						preparedStatement.setString(12, separacionDatosInsert[7]); // cantidad_documentos
						preparedStatement.setString(13, separacionDatosInsert[8]); // medio_recepcion
						preparedStatement.setString(14, separacionDatosInsert[9]); // radicacion_proceso
						preparedStatement.setString(15, separacionDatosInsert[10]); // correo_radicacion
						preparedStatement.setInt(16, Integer.parseInt(separacionDatosInsert[11])); // linea_negocio_id
						preparedStatement.setInt(17, 1); // formulario_sarlaft
						preparedStatement.setInt(18, 1); // formulario_repetido

						preparedStatement.executeUpdate();

						correoUser = separacionDatosInsert[9];
						rs = preparedStatement.getGeneratedKeys();
						if (rs.next()) {
							idRadicacion = rs.getInt(1);
							System.out.println("ID idRadicacion:  " + idRadicacion);
						}
						clienteIdentCompleta = Integer.parseInt(separacionDatosInsert[1]);
						if (Integer.parseInt(separacionDatosInsert[1]) == 3
								|| Integer.parseInt(separacionDatosInsert[1]) == 9) {

							String sqlClienteJuridico = "INSERT INTO `asistemyca_zurich`.`cliente_sarlaft_juridico` ("
									+ "`cliente`, `radicacion_observacion`, `ciudad_diligenciamiento`, `sucursal`, `tipo_solicitud`, `residencia_sociedad`, `clase_vinculacion`,"
									+ " `clase_vinculacion_otro`, `relacion_tom_asegurado`, `relacion_tom_asegurado_otra`, `relacion_tom_beneficiario`, "
									+ "`relacion_tom_beneficiario_otra`,`relacion_aseg_beneficiario`, `relacion_aseg_beneficiario_otra`, `razon_social`, `info_basica_tipo_sociedad`,"
									+ " `ofi_principal_direccion`, `ofi_principal_tipo_empresa`, `ofi_principal_departamento_empresa`, `ofi_principal_ciudad_empresa`, "
									+ "`ofi_principal_telefono`, `ofi_principal_fax`, `ofi_principal_pagina_web`, `ofi_principal_email`, `ofi_principal_ciiu`, "
									+ "`ofi_principal_ciiu_cod`, `ofi_principal_sector`, `sucursal_direccion`, `sucursal_departamento`, `sucursal_ciudad`,"
									+ " `sucursal_telefono`,  `rep_legal_primer_apellido`, `rep_legal_segundo_apellido`, `rep_legal_nombres`, `rep_legal_tipo_documento`,"
									+ " `rep_legal_documento`, `rep_legal_fecha_exp_documento`, `rep_legal_lugar_expedicion`, `rep_legal_fecha_nacimiento`, "
									+ "`rep_legal_lugar_nacimiento`, `rep_legal_nacionalidad_1`, `rep_legal_email`, `rep_legal_direccion_residencia`, "
									+ "`rep_legal_pais_residencia`, `rep_legal_departamento_residencia`, `rep_legal_ciudad_residencia`, `rep_legal_telefono_residencia`,"
									+ " `rep_legal_celular_residencia`, `rep_legal_persona_publica`, `rep_legal_recursos_publicos`, `rep_legal_obligaciones_tributarias`,"
									+ " `rep_legal_obligaciones_tributarias_indique`, `anexo_accionistas`, `anexo_sub_accionistas`, `ingresos`,"
									+ " `egresos`, `activos`, `pasivos`, `patrimonio`, `otros_ingresos`, `desc_otros_ingresos`,`tipo_moneda`, `anexo_preguntas_ppes`, "
									+ "`operaciones_moneda_extranjera`, `cuentas_moneda_exterior`, `productos_exterior`, `reclamaciones`, "
									+ "`reclamacion_anio`, `reclamacion_ramo`, `reclamacion_compania`, `reclamacion_valor`,`reclamacion_resultado`, `reclamacion_anio_2`,"
									+ " `reclamacion_ramo_2`, `reclamacion_compania_2`, `reclamacion_valor_2`, `reclamacion_resultado_2`, `chk_formulario_sarlaft`, "
									+ "`chk_documentos`, `otro`, `huella`, `firma`, `entrevista`, `verificacion`, `autoriza_info_fasecolda`, `autoriza_tratamiento`) "
									+ "VALUES ("
									+ "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
									+ "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
									+ "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
									+ "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
									+ "?,?,?,?,?,?) ";

							preparedStatement = conexion.prepareStatement(sqlClienteJuridico); // for insert

							if(separacionDatosInsert[13].equalsIgnoreCase("")) {
								separacionDatosInsert[13] = "0";
								System.out.println("estado_civil:  " + separacionDatosInsert[13]);
							}
							if(separacionDatosInsert[26].equalsIgnoreCase("")) {
								separacionDatosInsert[26] = "0";
								System.out.println("estado_civil:  " + separacionDatosInsert[26]);
							}
							if(separacionDatosInsert[29].equalsIgnoreCase("")) {
								separacionDatosInsert[29] = "0";
								System.out.println("estado_civil:  " + separacionDatosInsert[29]);
							}
							if(separacionDatosInsert[30].equalsIgnoreCase("")) {
								separacionDatosInsert[30] = "0";
								System.out.println("estado_civil:  " + separacionDatosInsert[30]);
							}
							if(separacionDatosInsert[31].equalsIgnoreCase("")) {
								separacionDatosInsert[31] = "0";
								System.out.println("estado_civil:  " + separacionDatosInsert[31]);
							}
							if(separacionDatosInsert[32].equalsIgnoreCase("")) {
								separacionDatosInsert[32] = "0";
								System.out.println("estado_civil:  " + separacionDatosInsert[32]);
							}
							if(separacionDatosInsert[37].equalsIgnoreCase("")) {
								separacionDatosInsert[37] = "0";
								System.out.println("estado_civil:  " + separacionDatosInsert[37]);
							}
							if(separacionDatosInsert[39].equalsIgnoreCase("")) {
								separacionDatosInsert[39] = "0";
								System.out.println("estado_civil:  " + separacionDatosInsert[39]);
							}
							if(separacionDatosInsert[39].equalsIgnoreCase("")) {
								separacionDatosInsert[39] = "0";
								System.out.println("estado_civil:  " + separacionDatosInsert[39]);
							}
							if(separacionDatosInsert[58].equalsIgnoreCase("")) {
								separacionDatosInsert[58] = "0";
								System.out.println("estado_civil:  " + separacionDatosInsert[58]);
							}
							if(separacionDatosInsert[111].equalsIgnoreCase("")) {
								separacionDatosInsert[111] = "0";
								System.out.println("estado_civil:  " + separacionDatosInsert[111]);
							}
							if(separacionDatosInsert[114].equalsIgnoreCase("")) {
								separacionDatosInsert[114] = "0";
								System.out.println("estado_civil:  " + separacionDatosInsert[114]);
							}
							if(separacionDatosInsert[115].equalsIgnoreCase("")) {
								separacionDatosInsert[115] = "0";
								System.out.println("estado_civil:  " + separacionDatosInsert[115]);
							}
							if(separacionDatosInsert[116].equalsIgnoreCase("")) {
								separacionDatosInsert[116] = "0";
								System.out.println("estado_civil:  " + separacionDatosInsert[116]);
							}
							if(separacionDatosInsert[119].equalsIgnoreCase("")) {
								separacionDatosInsert[119] = "0";
								System.out.println("estado_civil:  " + separacionDatosInsert[119]);
							}
							if(separacionDatosInsert[120].equalsIgnoreCase("")) {
								separacionDatosInsert[120] = "0";
								System.out.println("estado_civil:  " + separacionDatosInsert[120]);
							}
							if(separacionDatosInsert[128].equalsIgnoreCase("")) {
								separacionDatosInsert[128] = "0";
								System.out.println("estado_civil:  " + separacionDatosInsert[128]);
							}
							
//							,,,,,,,,40,41,45,51,54 55 56 57  64 93 94 95 96 97 98 106 110      
							preparedStatement.setInt(1, idClient); // cliente
							preparedStatement.setString(2, separacionDatosInsert[12]); // radicacion_observacion
							preparedStatement.setInt(3, Integer.parseInt(separacionDatosInsert[13])); // ciudad_diligenciamiento
							preparedStatement.setString(4, separacionDatosInsert[14]); // sucursal
							preparedStatement.setString(5, separacionDatosInsert[15]); // tipo_solicitud
							preparedStatement.setString(6, separacionDatosInsert[16]); // residencia_sociedad
							preparedStatement.setString(7, separacionDatosInsert[17]); // clase_vinculacion
							preparedStatement.setString(8, separacionDatosInsert[18]); // clase_vinculacion_otro
							preparedStatement.setString(9, separacionDatosInsert[19]); // relacion_tom_asegurado
							preparedStatement.setString(10, separacionDatosInsert[20]); // relacion_tom_asegurado_otra
							preparedStatement.setString(11, separacionDatosInsert[21]); // relacion_tom_beneficiario
							preparedStatement.setString(12, separacionDatosInsert[22]); // relacion_tom_beneficiario_otra
							preparedStatement.setString(13, separacionDatosInsert[23]); // relacion_aseg_beneficiario
							preparedStatement.setString(14, separacionDatosInsert[24]); // relacion_aseg_beneficiario_otra
							preparedStatement.setString(15, separacionDatosInsert[25]); // razon_social
							preparedStatement.setInt(16, Integer.parseInt(separacionDatosInsert[26])); // info_basica_tipo_sociedad
							preparedStatement.setString(17, separacionDatosInsert[27]); // ofi_principal_direccion
							preparedStatement.setString(18, separacionDatosInsert[28]); // ofi_principal_tipo_empresa
							preparedStatement.setInt(19, Integer.parseInt(separacionDatosInsert[29])); // ofi_principal_departamento_empresa
							preparedStatement.setInt(20, Integer.parseInt(separacionDatosInsert[30])); // ofi_principal_ciudad_empresa
							preparedStatement.setInt(21, Integer.parseInt(separacionDatosInsert[31])); // ofi_principal_telefono
							preparedStatement.setInt(22, Integer.parseInt(separacionDatosInsert[32])); // ofi_principal_fax
							preparedStatement.setString(23, separacionDatosInsert[33]); // ofi_principal_pagina_web
							preparedStatement.setString(24, separacionDatosInsert[34]); // ofi_principal_email
							preparedStatement.setString(25, separacionDatosInsert[35]); // ofi_principal_ciiu
							preparedStatement.setString(26, separacionDatosInsert[36]); // ofi_principal_ciiu_cod
							preparedStatement.setInt(27, Integer.parseInt(separacionDatosInsert[37])); // ofi_principal_sector
							preparedStatement.setString(28, separacionDatosInsert[38]); // sucursal_direccion
							preparedStatement.setInt(29, Integer.parseInt(separacionDatosInsert[39])); // sucursal_departamento
							preparedStatement.setInt(30, Integer.parseInt(separacionDatosInsert[40])); // sucursal_ciudad
							preparedStatement.setInt(31, Integer.parseInt(separacionDatosInsert[41])); // sucursal_telefono
							preparedStatement.setString(32, separacionDatosInsert[42]); // rep_legal_primer_apellido
							preparedStatement.setString(33, separacionDatosInsert[43]); // rep_legal_segundo_apellido
							preparedStatement.setString(34, separacionDatosInsert[44]); // rep_legal_nombres
							preparedStatement.setInt(35, Integer.parseInt(separacionDatosInsert[45])); // rep_legal_tipo_documento
							preparedStatement.setString(36, separacionDatosInsert[46]); // rep_legal_documento
							preparedStatement.setString(37, separacionDatosInsert[47]); // rep_legal_fecha_exp_documento
							preparedStatement.setString(38, separacionDatosInsert[48]); // rep_legal_lugar_expedicion
							preparedStatement.setString(39, separacionDatosInsert[49]); // rep_legal_fecha_nacimiento
							preparedStatement.setString(40, separacionDatosInsert[50]); // rep_legal_lugar_nacimiento
							preparedStatement.setInt(41, Integer.parseInt(separacionDatosInsert[51])); // rep_legal_nacionalidad_1
							preparedStatement.setString(42, separacionDatosInsert[52]); // rep_legal_email
							preparedStatement.setString(43, separacionDatosInsert[53]); // rep_legal_direccion_residencia
							preparedStatement.setInt(44, Integer.parseInt(separacionDatosInsert[54])); // rep_legal_pais_residencia
							preparedStatement.setInt(45, Integer.parseInt(separacionDatosInsert[55])); // rep_legal_departamento_residencia
							preparedStatement.setInt(46, Integer.parseInt(separacionDatosInsert[56])); // rep_legal_ciudad_residencia
							preparedStatement.setInt(47, Integer.parseInt(separacionDatosInsert[57])); // rep_legal_telefono_residencia
							preparedStatement.setInt(48, Integer.parseInt(separacionDatosInsert[58])); // rep_legal_celular_residencia
							preparedStatement.setString(49, separacionDatosInsert[59]); // rep_legal_persona_publica
							preparedStatement.setString(50, separacionDatosInsert[60]); // rep_legal_recursos_publicos
							preparedStatement.setString(51, separacionDatosInsert[61]); // rep_legal_obligaciones_tributarias
							preparedStatement.setString(52, separacionDatosInsert[62]); // rep_legal_obligaciones_tributarias_indique
							preparedStatement.setString(53, separacionDatosInsert[63]); // anexo_accionistas
							preparedStatement.setInt(54, Integer.parseInt(separacionDatosInsert[64])); // anexo_sub_accionistas
							preparedStatement.setInt(55, Integer.parseInt(separacionDatosInsert[93])); // ingresos
							preparedStatement.setInt(56, Integer.parseInt(separacionDatosInsert[94])); // egresos
							preparedStatement.setInt(57, Integer.parseInt(separacionDatosInsert[95])); // activos
							preparedStatement.setInt(58, Integer.parseInt(separacionDatosInsert[96])); // pasivos
							preparedStatement.setInt(59, Integer.parseInt(separacionDatosInsert[97])); // patrimonio
							preparedStatement.setInt(60, Integer.parseInt(separacionDatosInsert[98])); // otros_ingresos
							preparedStatement.setString(61, separacionDatosInsert[99]); // desc_otros_ingresos
							preparedStatement.setString(62, separacionDatosInsert[100]); // tipo_moneda
							preparedStatement.setInt(63, Integer.parseInt(separacionDatosInsert[106])); // anexo_preguntas_ppes
							preparedStatement.setString(64, separacionDatosInsert[107]); // operaciones_moneda_extranjera
							preparedStatement.setString(65, separacionDatosInsert[108]); // cuentas_moneda_exterior
							preparedStatement.setString(66, separacionDatosInsert[109]); // productos_exterior
							preparedStatement.setInt(67, Integer.parseInt(separacionDatosInsert[110])); // reclamaciones
							preparedStatement.setInt(68, Integer.parseInt(separacionDatosInsert[111])); // reclamacion_anio
							preparedStatement.setString(69, separacionDatosInsert[112]); // reclamacion_ramo
							preparedStatement.setString(70, separacionDatosInsert[113]); // reclamacion_compania
							preparedStatement.setInt(71, Integer.parseInt(separacionDatosInsert[114])); // reclamacion_valor
							preparedStatement.setInt(72, Integer.parseInt(separacionDatosInsert[115])); // reclamacion_resultado
							preparedStatement.setString(73, separacionDatosInsert[116]); // reclamacion_anio_2
							preparedStatement.setString(74, separacionDatosInsert[117]); // reclamacion_ramo_2
							preparedStatement.setString(75, separacionDatosInsert[118]); // reclamacion_compania_2
							preparedStatement.setInt(76, Integer.parseInt(separacionDatosInsert[119])); // reclamacion_valor_2
							preparedStatement.setInt(77, Integer.parseInt(separacionDatosInsert[120])); // reclamacion_resultado_2
							preparedStatement.setInt(78, Integer.parseInt(separacionDatosInsert[121])); // chk_formulario_sarlaft
							preparedStatement.setInt(79, Integer.parseInt(separacionDatosInsert[122])); // chk_documentos
							preparedStatement.setInt(80, Integer.parseInt(separacionDatosInsert[123])); // otro
							preparedStatement.setInt(81, Integer.parseInt(separacionDatosInsert[125])); // huella
							preparedStatement.setInt(82, Integer.parseInt(separacionDatosInsert[126])); // firma
							preparedStatement.setInt(83, Integer.parseInt(separacionDatosInsert[127])); // entrevista
							preparedStatement.setInt(84, Integer.parseInt(separacionDatosInsert[128])); // verificacion
							preparedStatement.setString(85, separacionDatosInsert[129]); // autoriza_info_fasecolda
							preparedStatement.setString(86, separacionDatosInsert[129]); // autoriza_tratamiento
							preparedStatement.executeUpdate();
						}

						else {
							String sqlClienteNatural = "INSERT INTO `asistemyca_zurich`.`cliente_sarlaft_natural` ("
									+ "`cliente`, `radicacion_observacion`, `ciudad_diligenciamiento`, `sucursal`, `tipo_solicitud`, `clase_vinculacion`, "
									+ "`clase_vinculacion_otro`, `relacion_tom_asegurado`,"
									+ "`relacion_tom_asegurado_otra`, `relacion_tom_beneficiario`,`relacion_tom_beneficiario_otra`, `relacion_aseg_beneficiario`,"
									+ " `relacion_aseg_beneficiario_otra`, `primer_apellido`, `segundo_apellido`, `primer_nombre`,"
									+ " `segundo_nombre`, `sexo`, `estado_civil`, `fecha_expedicion_documento`, `lugar_expedicion_documento`, `fecha_nacimiento`, "
									+ "`lugar_nacimiento`, `nacionalidad_1`, `ocupacion`, `direccion_residencia`, `departamento_residencia`, "
									+ "`ciudad_residencia`, `telefono`, `celular`, `correo_electronico`, `actividad_eco_principal`, `trabaja_actualmente`,"
									+ " `sector`, `tipo_actividad`, `cargo`, `empresa_donde_trabaja`, `departamento_empresa`,"
									+ " `ciudad_empresa`, `direccion_empresa`, `telefono_empresa`, `ingresos`, `egresos`, `activos`, `pasivos`, `patrimonio`, "
									+ "`otros_ingresos`, `desc_otros_ingresos`, `tipo_moneda`, `persona_publica`, `vinculo_persona_publica`, `productos_publicos`,"
									+ " `obligaciones_tributarias_otro_pais`, `desc_obligaciones_tributarias_otro_pais`, `anexo_preguntas_ppes`, "
									+ "`operaciones_moneda_extranjera`, `cuentas_moneda_exterior`, `productos_exterior`, `reclamaciones`, `reclamacion_anio`, "
									+ "`reclamacion_ramo`, `reclamacion_compania`, `reclamacion_valor`, `reclamacion_resultado`, `reclamacion_anio_2`, `reclamacion_ramo_2`,"
									+ " `reclamacion_compania_2`, `reclamacion_valor_2`, `reclamacion_resultado_2`, `chk_formulario_sarlaft`, `chk_documentos`, `otro`,"
									+ " `tipo_documento_otro`, `huella`, `firma`, `entrevista`, `verificacion`, `autoriza_info_fasecolda`, `autoriza_tratamiento`)"
									+ "VALUES (" + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
									+ "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
									+ "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
									+ "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";

							preparedStatement = conexion.prepareStatement(sqlClienteNatural); // for insert
							// set values
							if(separacionDatosInsert[70].equalsIgnoreCase("")) {
								separacionDatosInsert[70] = "0";
								System.out.println("estado_civil:  " + separacionDatosInsert[70]);
							}
							if(separacionDatosInsert[80].equalsIgnoreCase("")) {
								separacionDatosInsert[80] = "000000";
								System.out.println("telefono:  " + separacionDatosInsert[80]);
							}
							if(separacionDatosInsert[81].equalsIgnoreCase("")) {
								separacionDatosInsert[81] = "0";
								System.out.println("celular:  " + separacionDatosInsert[81]);
							}
							if(separacionDatosInsert[92].equalsIgnoreCase("")) {
								separacionDatosInsert[92] = "000000";
								System.out.println("telefono:  " + separacionDatosInsert[92]);
							}
							if(separacionDatosInsert[114].equalsIgnoreCase("")) {
								separacionDatosInsert[114] = "0";
								System.out.println("telefono:  " + separacionDatosInsert[114]);
							}
							if(separacionDatosInsert[115].equalsIgnoreCase("")) {
								separacionDatosInsert[115] = "0";
								System.out.println("telefono:  " + separacionDatosInsert[115]);
							}
							if(separacionDatosInsert[116].equalsIgnoreCase("")) {
								separacionDatosInsert[116] = "0";
								System.out.println("telefono:  " + separacionDatosInsert[116]);
							}
							if(separacionDatosInsert[119].equalsIgnoreCase("")) {
								separacionDatosInsert[119] = "0";
								System.out.println("telefono:  " + separacionDatosInsert[119]);
							}
							if(separacionDatosInsert[120].equalsIgnoreCase("")) {
								separacionDatosInsert[120] = "0";
								System.out.println("telefono:  " + separacionDatosInsert[119]);
							}
							if(separacionDatosInsert[128].equalsIgnoreCase("")) {
								separacionDatosInsert[128] = "0";
								System.out.println("telefono:  " + separacionDatosInsert[128]);
							}
							
							preparedStatement.setInt(1, idClient); // cliente|
							preparedStatement.setString(2, separacionDatosInsert[12]); // radicacion_observacion
							preparedStatement.setInt(3, Integer.parseInt(separacionDatosInsert[13])); // ciudad_diligenciamiento|
							preparedStatement.setString(4, separacionDatosInsert[14]); // sucursal
							preparedStatement.setString(5, separacionDatosInsert[15]);// tipo_solicitud
							preparedStatement.setString(6, separacionDatosInsert[17]); // clase_vinculacion
							preparedStatement.setString(7, separacionDatosInsert[18]); // clase_vinculacion_otro
							preparedStatement.setString(8, separacionDatosInsert[19]); // relacion_tom_asegurado
							preparedStatement.setString(9, separacionDatosInsert[20]); // relacion_tom_asegurado_otra
							preparedStatement.setString(10, separacionDatosInsert[21]); // relacion_tom_beneficiario
							preparedStatement.setString(11, separacionDatosInsert[22]); // relacion_tom_beneficiario_otra
							preparedStatement.setString(12, separacionDatosInsert[23]); // relacion_aseg_beneficiario
							preparedStatement.setString(13, separacionDatosInsert[24]); // relacion_aseg_beneficiario_otra
							preparedStatement.setString(14, separacionDatosInsert[65]); // primer_apellido
							preparedStatement.setString(15, separacionDatosInsert[66].toString()); // segundo_apellido
							preparedStatement.setString(16, separacionDatosInsert[67].toString()); // primer_nombre
							preparedStatement.setString(17, separacionDatosInsert[68].toString()); // segundo_nombre
							preparedStatement.setString(18, separacionDatosInsert[69]); // sexo
							preparedStatement.setInt(19, Integer.parseInt(separacionDatosInsert[70])); // estado_civil
							preparedStatement.setString(20, separacionDatosInsert[71]); // fecha_expedicion_documento
							preparedStatement.setString(21, separacionDatosInsert[72]); // lugar_expedicion_documento
							preparedStatement.setString(22, separacionDatosInsert[73]); // fecha_nacimiento
							preparedStatement.setString(23, separacionDatosInsert[74]); // lugar_nacimiento
							preparedStatement.setInt(24, Integer.parseInt(separacionDatosInsert[75])); // nacionalidad_1
							preparedStatement.setString(25, separacionDatosInsert[76]); // ocupacion
							preparedStatement.setString(26, separacionDatosInsert[77]); // direccion_residencia
							preparedStatement.setInt(27, Integer.parseInt(separacionDatosInsert[78])); // departamento_residencia
							preparedStatement.setInt(28, Integer.parseInt(separacionDatosInsert[79])); // ciudad_residencia
							preparedStatement.setInt(29, Integer.parseInt(separacionDatosInsert[80])); // telefono
							preparedStatement.setInt(30, Integer.parseInt(separacionDatosInsert[81])); // celular
							preparedStatement.setString(31, separacionDatosInsert[82]); // correo_electronico
							preparedStatement.setString(32, separacionDatosInsert[83]); // actividad_eco_principal
//							preparedStatement.setInt(27, Integer.parseInt(separacionDatosInsert[actividad_eco_principal])); // ciiu_cod
							preparedStatement.setInt(33, Integer.parseInt(separacionDatosInsert[84])); // trabaja_actualmente
							preparedStatement.setInt(34, Integer.parseInt(separacionDatosInsert[85])); // sector
							preparedStatement.setInt(35, Integer.parseInt(separacionDatosInsert[86])); // tipo_actividad
							preparedStatement.setString(36, separacionDatosInsert[87]); // cargo
							preparedStatement.setString(37, separacionDatosInsert[88]); // empresa_donde_trabaja
							preparedStatement.setInt(38, Integer.parseInt(separacionDatosInsert[89])); // departamento_empresa
							preparedStatement.setInt(39, Integer.parseInt(separacionDatosInsert[90])); // ciudad_empresa
							preparedStatement.setString(40, separacionDatosInsert[91]); // direccion_empresa
							preparedStatement.setInt(41, Integer.parseInt(separacionDatosInsert[92])); // telefono_empresa
							preparedStatement.setInt(42, Integer.parseInt(separacionDatosInsert[93])); // ingresos
							preparedStatement.setInt(43, Integer.parseInt(separacionDatosInsert[94])); // egresos
							preparedStatement.setInt(44, Integer.parseInt(separacionDatosInsert[95])); // activos
							preparedStatement.setInt(45, Integer.parseInt(separacionDatosInsert[96])); // pasivos
							preparedStatement.setInt(46, Integer.parseInt(separacionDatosInsert[97])); // patrimonio
							preparedStatement.setInt(47, Integer.parseInt(separacionDatosInsert[98])); // otros_ingresos
							preparedStatement.setString(48, separacionDatosInsert[99]); // desc_otros_ingresos
							preparedStatement.setString(49, separacionDatosInsert[100]); // tipo_moneda
							preparedStatement.setString(50, separacionDatosInsert[101]); // persona_publica
							preparedStatement.setString(51, separacionDatosInsert[102]); // vinculo_persona_publica
							preparedStatement.setString(52, separacionDatosInsert[103]); // productos_publicos
							preparedStatement.setString(53, separacionDatosInsert[104]); // obligaciones_tributarias_otro_pais
							preparedStatement.setString(54, separacionDatosInsert[105]); // desc_obligaciones_tributarias_otro_pais
							preparedStatement.setInt(55, Integer.parseInt(separacionDatosInsert[106])); // anexo_preguntas_ppes
							preparedStatement.setString(56, separacionDatosInsert[107]); // operaciones_moneda_extranjera
							preparedStatement.setString(57, separacionDatosInsert[108]); // cuentas_moneda_exterior
							preparedStatement.setString(58, separacionDatosInsert[109]); // productos_exterior
							preparedStatement.setInt(59, Integer.parseInt(separacionDatosInsert[110])); // reclamaciones
							preparedStatement.setInt(60, Integer.parseInt(separacionDatosInsert[111])); // reclamacion_anio
							preparedStatement.setString(61, separacionDatosInsert[112]); // reclamacion_ramo
							preparedStatement.setString(62, separacionDatosInsert[113]); // reclamacion_compania
							preparedStatement.setString(63, separacionDatosInsert[114]); // reclamacion_valor    x
							preparedStatement.setInt(64, Integer.parseInt(separacionDatosInsert[115])); // reclamacion_resultado    x
							preparedStatement.setInt(65, Integer.parseInt(separacionDatosInsert[116])); // reclamacion_anio_2      x
							preparedStatement.setString(66, separacionDatosInsert[117]); // reclamacion_ramo_2
							preparedStatement.setString(67, separacionDatosInsert[118]); // reclamacion_compania_2
							preparedStatement.setInt(68, Integer.parseInt(separacionDatosInsert[119])); // reclamacion_valor_2        x      
							preparedStatement.setInt(69, Integer.parseInt(separacionDatosInsert[120])); // reclamacion_resultado_2
							preparedStatement.setInt(70, Integer.parseInt(separacionDatosInsert[121])); // chk_formulario_sarlaft
							preparedStatement.setInt(71, Integer.parseInt(separacionDatosInsert[122])); // chk_documentos
							preparedStatement.setInt(72, Integer.parseInt(separacionDatosInsert[123])); // otro
							preparedStatement.setString(73, separacionDatosInsert[124]); // tipo_documento_otro
							preparedStatement.setInt(74, Integer.parseInt(separacionDatosInsert[125])); // huella
							preparedStatement.setInt(75, Integer.parseInt(separacionDatosInsert[126])); // firma
							preparedStatement.setInt(76, Integer.parseInt(separacionDatosInsert[127])); // entrevista
							preparedStatement.setInt(77, Integer.parseInt(separacionDatosInsert[128])); // verificacion
							preparedStatement.setString(78, separacionDatosInsert[129]); // autoriza_info_fasecolda
							preparedStatement.setString(79, separacionDatosInsert[130]); // autoriza_tratamiento
                            
							preparedStatement.executeUpdate();

						}

					}
					if (separacionDatosInsert[0].equals("R2")) {

						String sqlArchivoOrganizado = "INSERT INTO `asistemyca_zurich`.`relacion_archivo_radicacion` (`RADICACION_ID`, `CLIENTE_ID`, `NOMBRE_ARCHIVO`) "
								+ "VALUES (?,?,?)";

						preparedStatement = conexion.prepareStatement(sqlArchivoOrganizado); // for insert

						preparedStatement.setInt(1, idRadicacion); // RADICACION_ID
						preparedStatement.setInt(2, idClient); // CLIENTE_ID
						preparedStatement.setString(3, separacionDatosInsert[2]); // NOMBRE_ARCHIVO

						preparedStatement.executeUpdate();

					}

					if (clienteIdentCompleta == 3 || clienteIdentCompleta == 9) {

						if (separacionDatosInsert[0].equals("R3")) {

							// falta acomodar un replace por si llega a venir con ( , )

							String sqlAccionistas = "INSERT INTO `asistemyca_zurich`.`accionistas` (`cliente_id`,`accionista_tipo_documento`, `accionista_documento`, `accionista_nombres_completos`, `accionista_participacion`, `accionista_cotiza_bolsa`, `accionista_persona_publica`, `accionista_obligaciones_otro_pais`, `accionista_obligaciones_otro_pais_desc`, `created`) "
									+ "VALUES (?,?,?,?,CAST(? AS DECIMAL(20,2)),?,?,?,?,?)";

							preparedStatement = conexion.prepareStatement(sqlAccionistas); // for insert
							
							if(separacionDatosInsert[1].equalsIgnoreCase("")) {
								separacionDatosInsert[1] = "0";
								System.out.println("accionista_tipo_documento:  " + separacionDatosInsert[1]);
							}
							if(separacionDatosInsert[4].equalsIgnoreCase("")) {
								separacionDatosInsert[4] = "0";
								System.out.println("accionista_participacion:  " + separacionDatosInsert[4]);
							}
							if(separacionDatosInsert[8].equalsIgnoreCase("")) {
								separacionDatosInsert[8] = "null";
								System.out.println("accionista_obligaciones_otro_pais_desc:  " + separacionDatosInsert[8]);
							}
							preparedStatement.setInt(1, idClient); // cliente_id
							preparedStatement.setInt(2, Integer.parseInt(separacionDatosInsert[1])); // accionista_tipo_documento
							preparedStatement.setString(3, separacionDatosInsert[2]); // accionista_documento
							preparedStatement.setString(4, separacionDatosInsert[3]); // accionista_nombres_completos
							preparedStatement.setString(5, separacionDatosInsert[4]); // accionista_participacion
							preparedStatement.setString(6, separacionDatosInsert[5]);// accionista_cotiza_bolsa
							preparedStatement.setString(7, separacionDatosInsert[6]); // accionista_persona_publica
							preparedStatement.setString(8, separacionDatosInsert[7]); // accionista_obligaciones_otro_pais
							preparedStatement.setString(9, separacionDatosInsert[8]); // accionista_obligaciones_otro_pais_desc
							preparedStatement.setString(10, dateFormat.format(date).toString()); // created

							preparedStatement.executeUpdate();

						}
						if (separacionDatosInsert[0].equals("R4")) {

							// falta acomodar un replace por si llega a venir con ( , )

							String sqlSubAccionistas = "INSERT INTO `asistemyca_zurich`.`sub_accionistas` (`cliente_id`, `sub_accionista_tipo_documento`, `sub_accionista_numero_id`, `sub_accionista_razon_social`, `sub_accionista_participacion`, `sub_accionista_nombre_sociedad_accionista`, `sub_accionista_documento`, `created`) "
									+ "VALUES (?,?,?,?,CAST(? AS DECIMAL(20,2)),?,?,?)";

							preparedStatement = conexion.prepareStatement(sqlSubAccionistas); // for insert

							if(separacionDatosInsert[1].equalsIgnoreCase("")) {
								separacionDatosInsert[1] = "0";
							}
							if(separacionDatosInsert[4].equalsIgnoreCase("")) {
								separacionDatosInsert[4] = "0";
							}
							if(separacionDatosInsert[6].equalsIgnoreCase("")) {
								separacionDatosInsert[6] = "null";
							}
							preparedStatement.setInt(1, idClient); // cliente_id
							preparedStatement.setInt(2, Integer.parseInt(separacionDatosInsert[1])); // sub_accionista_tipo_documento
							preparedStatement.setString(3, separacionDatosInsert[2]); // sub_accionista_numero_id
							preparedStatement.setString(4, separacionDatosInsert[3]); // sub_accionista_razon_social
							preparedStatement.setString(5, separacionDatosInsert[4]); // sub_accionista_participacion
							preparedStatement.setString(6, separacionDatosInsert[5]);// sub_accionista_nombre_sociedad_accionista
							preparedStatement.setString(7, separacionDatosInsert[6]); // sub_accionista_documento
							preparedStatement.setString(8, dateFormat.format(date).toString()); // created
							preparedStatement.executeUpdate();

						}
					}

					if (separacionDatosInsert[0].equals("R5")) {

						String sqlAnexosPpes = "INSERT INTO `asistemyca_zurich`.`zr_anexos_ppes` (`cliente_id`, `ppes_vinculo_relacion`, `ppes_nombre`, `ppes_tipo_identificacion`, `ppes_no_documento`, `ppes_nacionalidad`, `ppes_entidad`, `ppes_cargo`, `ppes_fecha_ingreso` , `ppes_desvinculacion`, `ppes_motivo` ,`created`)"
								+ " VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";

						preparedStatement = conexion.prepareStatement(sqlAnexosPpes); // for insert

						if(separacionDatosInsert[8].equalsIgnoreCase("")) {
							separacionDatosInsert[8] = "n";
							System.out.println("ppes_fecha_ingreso:  " + separacionDatosInsert[8]);
						}
						preparedStatement.setInt(1, idClient); // cliente_id
						preparedStatement.setString(2, separacionDatosInsert[1]); // ppes_vinculo_relacion
						preparedStatement.setString(3, separacionDatosInsert[2]); // ppes_nombre
						preparedStatement.setInt(4, Integer.parseInt(separacionDatosInsert[3])); // ppes_tipo_identificacion
						preparedStatement.setString(5, separacionDatosInsert[4]); // ppes_no_documento
						preparedStatement.setInt(6, Integer.parseInt(separacionDatosInsert[5]));// ppes_nacionalidad
						preparedStatement.setString(7, separacionDatosInsert[6]); // ppes_entidad
						preparedStatement.setString(8, separacionDatosInsert[7]); // ppes_cargo
						preparedStatement.setString(9, separacionDatosInsert[8]); // ppes_fecha_ingreso
						preparedStatement.setString(10, separacionDatosInsert[9]); // ppes_desvinculacion
						preparedStatement.setString(11, separacionDatosInsert[10]); // ppes_motivo
						preparedStatement.setString(12, dateFormat.format(date).toString()); // created

						preparedStatement.executeUpdate();

					}
				} catch (Exception ex) {
					conexion.rollback();
					error = "error en los datos recibidos. Por favor revise la linea  " + '\n' + separacionDatos[i];
					conexion.close();
					System.out.println("cat1" + ex.getMessage());
					SendMail.enviarcorreo(error, nomArchivo, correoDestinatario.getCorreo());
					return false;
				}

			}
			// End of JDBC transaction
			conexion.commit(); // commit, if successful
			System.out.println("commit done !!");
			conexion.close();
			System.out.println(correoDestinatario.getCorreo());
			return true;

		} catch (Exception e) {
			error = "se ha producido un error al procesar el archivo. Por favor revise";
			conexion.rollback();
			conexion.close();
			System.out.println("ca2" + e.getMessage());
			SendMail.enviarcorreo(error, nomArchivo, correoDestinatario.getCorreo());
			return false;

		}

	}

	public static boolean verificacionArchivoBD(String nomArchivo) {
		int resultado = 0;
		PreparedStatement preparedStatement = null;
		// instanciamos la clase Connection la cual representa la conexión con la Base
		// de Datos
		Connection conexion;
		ConexionBD miConexion = new ConexionBD();
		// asignamos la conexión a nuesta BD
		conexion = miConexion.getConnection();
		ResultSet rs;

		try {
			conexion.setAutoCommit(true);
			preparedStatement = conexion.prepareStatement(
					"SELECT COUNT(*) FROM asistemyca_zurich.relacion_archivo_radicacion where NOMBRE_ARCHIVO = '"
							+ nomArchivo + "'");
			preparedStatement.executeQuery();
			rs = preparedStatement.getResultSet();
			if (rs.next()) {
				resultado = rs.getInt(1);
				System.out.println("el count es  " + resultado);
			}
			conexion.close();
			if (resultado > 0) {
				return true;
			} else {
				return false;
			}

		} catch (Exception e) {
			return false;
		}

	}

	public String getCorreo() {
		return correo;
	}

	public void setCorreo(String correo) {
		this.correo = correo;
	}
	
	public void validacionR3(){
		
	}

}





