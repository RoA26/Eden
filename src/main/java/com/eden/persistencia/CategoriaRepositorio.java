package com.eden.persistencia;

import com.eden.modelo.Categoria;
import com.eden.modelo.TipoCategoria;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepositorio {

    void insertarTodas(List<Categoria> categorias);

    Long insertar(Categoria categoria);

    void actualizarNombre(Long idUsuario, Long id, String nombre);

    void cambiarEstado(Long idUsuario, Long id, boolean activa);

    Optional<Categoria> buscarPorId(Long idUsuario, Long id);

    List<Categoria> listar(Long idUsuario);

    boolean existeNombre(Long idUsuario, TipoCategoria tipo, String nombre, Long idExcluido);
}
