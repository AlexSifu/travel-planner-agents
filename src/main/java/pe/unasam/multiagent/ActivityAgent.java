package pe.unasam.multiagent;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.util.HashMap;
import java.util.Map;

public class ActivityAgent extends Agent {

    private static final Map<String, String> activitiesByCity = new HashMap<>();

    static {
        activitiesByCity.put("huaraz",
                "Trekking en la Cordillera Blanca, laguna 69, visita a Chavin.");
        activitiesByCity.put("lima",
                "Circuito de playas, centro historico, gastronomia en Miraflores y Barranco.");
        activitiesByCity.put("cusco",
                "City tour, valle sagrado, Machu Picchu, caminatas cortas.");
        activitiesByCity.put("arequipa",
                "Monasterio de Santa Catalina, miradores y Cañon del Colca.");
        activitiesByCity.put("iquitos",
                "Paseos por el rio Amazonas, lodges en la selva, observacion de fauna.");
        activitiesByCity.put("puno",
                "Islas de los Uros y Taquile, navegacion en el lago Titicaca.");
        activitiesByCity.put("paracas",
                "Islas Ballestas, reserva nacional de Paracas, deportes nauticos.");
        activitiesByCity.put("mancora",
                "Playas, surf, vida nocturna y relax en hoteles frente al mar.");
        activitiesByCity.put("trujillo",
                "Chan Chan, Huaca del Sol y de la Luna, playas de Huanchaco.");
        activitiesByCity.put("nazca",
                "Sobrevuelo de las lineas de Nazca, miradores y museo local.");
    }

    @Override
    protected void setup() {
        System.out.println(getLocalName() + " (ActivityAgent) iniciado");
        registerService("activity-service", "activity-recommender");

        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = myAgent.receive();
                if (msg != null) {
                    if (msg.getPerformative() == ACLMessage.REQUEST) {

                        String content = msg.getContent(); // ciudad;dias
                        String[] parts = content.split(";");
                        String city = parts[0];

                        String normalized = city.trim().toLowerCase();

                        String activities = activitiesByCity.get(normalized);
                        if (activities == null) {
                            activities = "Actividades genericas: city tour, gastronomia local y visitas a atractivos cercanos.";
                        }

                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent(activities);
                        reply.setConversationId(msg.getConversationId());
                        send(reply);

                        System.out.println(getLocalName() +
                                " recomendo actividades para " + city);
                    }
                } else {
                    block();
                }
            }
        });
    }

    private void registerService(String type, String name) {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(type);
        sd.setName(name);
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }
}
