package cl.empresa.depositocpc.mapper;

import cl.empresa.depositocpc.dto.ContenedorResponseDTO;
import cl.empresa.depositocpc.dto.UbicacionDTO;
import cl.empresa.depositocpc.entity.Contenedor;
import cl.empresa.depositocpc.entity.Ubicacion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ContenedorMapper {

    @Mapping(target = "ubicacion", source = "contenedor.ubicacion")
    @Mapping(target = "diasEnDeposito", source = "diasEnDeposito")
    @Mapping(target = "enAlerta", source = "enAlerta")
    ContenedorResponseDTO toDto(Contenedor contenedor, long diasEnDeposito, boolean enAlerta);

    UbicacionDTO toDto(Ubicacion ubicacion);
}
