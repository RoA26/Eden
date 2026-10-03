package com.eden.servicio;

import com.eden.dto.CategoriaForm;
import com.eden.excepcion.RecursoNoEncontradoException;
import com.eden.excepcion.ReglaNegocioException;
import com.eden.modelo.Categoria;
import com.eden.modelo.TipoCategoria;
import com.eden.persistencia.CategoriaRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CategoriaService {

    /** Categorias con las que arranca cada usuario nuevo; luego puede editarlas. */
    private static final Map<TipoCategoria, List<String>> CATEGORIAS_POR_DEFECTO = new LinkedHashMap<>();

    static {
        CATEGORIAS_POR_DEFECTO.put(TipoCategoria.INGRESO,
                List.of("Producido", "Salario", "Otros ingresos"));
        CATEGORIAS_POR_DEFECTO.put(TipoCategoria.GASTO,
                List.of("Alimentación", "Arriendo", "Servicios públicos", "Transporte", "Salud", "Ocio", "Otros gastos"));
        CATEGORIAS_POR_DEFECTO.put(TipoCategoria.COSTO_OPERATIVO,
                List.of("Combustible", "Mantenimiento", "Repuestos", "SOAT y documentos", "Otros costos"));
    }

    private final CategoriaRepositorio categoriaRepositorio;

    public CategoriaService(CategoriaRepositorio categoriaRepositorio) {
        this.categoriaRepositorio = categoriaRepositorio;
    }

    @Transactional
    public void crearCategoriasPorDefecto(Long idUsuario) {
        List<Categoria> categorias = new ArrayList<>();
        CATEGORIAS_POR_DEFECTO.forEach((tipo, nombres) ->
                nombres.forEach(nombre -> categorias.add(new Categoria(idUsuario, nombre, tipo))));
        categoriaRepositorio.insertarTodas(categorias);
    }

    public List<Categoria> listar(Long idUsuario) {
        return categoriaRepositorio.listar(idUsuario);
    }

    public List<Categoria> listarActivas(Long idUsuario, TipoCategoria... tipos) {
        List<TipoCategoria> buscados = List.of(tipos);
        return categoriaRepositorio.listar(idUsuario).stream()
                .filter(c -> c.isActiva() && buscados.contains(c.getTipo()))
                .toList();
    }

    public Categoria obtener(Long idUsuario, Long idCategoria) {
        return categoriaRepositorio.buscarPorId(idUsuario, idCategoria)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada"));
    }

    @Transactional
    public void crear(Long idUsuario, CategoriaForm formulario) {
        String nombre = formulario.getNombre().trim();
        validarNombreUnico(idUsuario, formulario.getTipo(), nombre, null);
        categoriaRepositorio.insertar(new Categoria(idUsuario, nombre, formulario.getTipo()));
    }

    @Transactional
    public void renombrar(Long idUsuario, Long idCategoria, String nuevoNombre) {
        if (nuevoNombre == null || nuevoNombre.isBlank()) {
            throw new ReglaNegocioException("El nombre de la categoría no puede quedar vacío.");
        }
        String nombre = nuevoNombre.trim();
        if (nombre.length() > 50) {
            throw new ReglaNegocioException("El nombre admite hasta 50 caracteres.");
        }
        Categoria categoria = obtener(idUsuario, idCategoria);
        validarNombreUnico(idUsuario, categoria.getTipo(), nombre, idCategoria);
        categoriaRepositorio.actualizarNombre(idUsuario, idCategoria, nombre);
    }

    /** Las categorias no se borran (tienen movimientos historicos): se desactivan. */
    @Transactional
    public void cambiarEstado(Long idUsuario, Long idCategoria, boolean activa) {
        obtener(idUsuario, idCategoria);
        categoriaRepositorio.cambiarEstado(idUsuario, idCategoria, activa);
    }

    private void validarNombreUnico(Long idUsuario, TipoCategoria tipo, String nombre, Long idExcluido) {
        if (categoriaRepositorio.existeNombre(idUsuario, tipo, nombre, idExcluido)) {
            throw new ReglaNegocioException("nombre", "Ya tienes una categoría de ese tipo con ese nombre.");
        }
    }
}
