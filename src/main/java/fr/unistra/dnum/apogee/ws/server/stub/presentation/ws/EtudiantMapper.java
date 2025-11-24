package fr.unistra.dnum.apogee.ws.server.stub.presentation.ws;

import gouv.education.apogee.commun.client.ws.EtudiantMetier.*;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EtudiantMapper {

    InfoAdmEtuDTO3 infoAdmEtuDTO3(InfoAdmEtuDTO4 etudiant);

    TableauIndBacDTO tableauIndBacDTO(TableauIndBacDTO2 tableauIndBacDTO2);

    IndBacDTO indBacDTO(IndBacDTO2 indBacDTO2);

}
