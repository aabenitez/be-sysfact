package com.marithe.sysfact.service;

import com.marithe.sysfact.exception.BusinessLogicException;
import com.marithe.sysfact.model.Parametro;
import com.marithe.sysfact.util.CalculadoraIva;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

/** Reglas de IVA compartidas por artículos y ventas. */
@Service
public class ImpuestoService {

    /** Parámetro con la tasa que se asigna a un artículo nuevo que no indica la suya. */
    public static final String PARAM_IVA_GENERAL = "impuesto.iva.general";

    private final Logger logger = Logger.getLogger(getClass().getName());

    @Autowired
    private ParametroService parametroService;

    /** Tasa del parámetro impuesto.iva.general (10, 5 o 0); 10 si falta o no es válida. */
    public int tasaGeneral() {
        try {
            Parametro p = parametroService.findByCodigo(PARAM_IVA_GENERAL);
            int tasa = Integer.parseInt(p.getValor().trim());
            if (esTasaValida(tasa)) {
                return tasa;
            }
            logger.warning("Valor no válido en " + PARAM_IVA_GENERAL + ": " + p.getValor() + ". Se usa 10 %.");
        } catch (Exception e) {
            logger.warning("No se pudo leer " + PARAM_IVA_GENERAL + " (" + e.getMessage() + "). Se usa 10 %.");
        }
        return CalculadoraIva.TASA_10;
    }

    public static boolean esTasaValida(Integer tasa) {
        return tasa != null
                && (tasa == CalculadoraIva.TASA_10 || tasa == CalculadoraIva.TASA_5 || tasa == CalculadoraIva.EXENTA);
    }

    public static void validarTasa(Integer tasa) {
        if (!esTasaValida(tasa)) {
            throw new BusinessLogicException("La tasa de IVA debe ser 10, 5 o 0 (exenta).");
        }
    }
}
