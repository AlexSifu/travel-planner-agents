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

public class WeatherAgent extends Agent {

    private static class WeatherProfile {
        final String summary;

        WeatherProfile(String summary) {
            this.summary = summary;
        }
    }

    // Map de ciudades turisticas mas importantes del Peru
    private static final Map<String, WeatherProfile> weatherByCity = new HashMap<>();

    static {
        weatherByCity.put("huaraz",   new WeatherProfile("CLIMA BUENO: dias soleados y noches frias en Huaraz."));
        weatherByCity.put("lima",     new WeatherProfile("CLIMA MIXTO: cielo cubierto en invierno y templado el resto del anio en Lima."));
        weatherByCity.put("cusco",    new WeatherProfile("CLIMA VARIABLE: sol fuerte en el dia y frio en la noche en Cusco."));
        weatherByCity.put("arequipa", new WeatherProfile("CLIMA SECO: dias templados y noches frescas en Arequipa."));
        weatherByCity.put("iquitos",  new WeatherProfile("CLIMA CALIDO Y HUMEDO: altas temperaturas y lluvias frecuentes en Iquitos."));
        weatherByCity.put("puno",     new WeatherProfile("CLIMA FRIO DE ALTURA: noches muy frias y vientos fuertes en Puno."));
        weatherByCity.put("paracas",  new WeatherProfile("CLIMA SECO Y VENTOSO: ideal para deportes nauticos en Paracas."));
        weatherByCity.put("mancora",  new WeatherProfile("CLIMA TROPICAL: sol casi todo el anio y temperatura calida en Mancora."));
        weatherByCity.put("trujillo", new WeatherProfile("CLIMA SUAVE: pocas lluvias y temperaturas agradables en Trujillo."));
        weatherByCity.put("nazca",    new WeatherProfile("CLIMA ARIDO: dias muy soleados y pocas lluvias en Nazca."));
    }

    @Override
    protected void setup() {
        System.out.println(getLocalName() + " (WeatherAgent) iniciado");
        registerService("weather-service", "weather-info");

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

                        WeatherProfile profile = weatherByCity.get(normalized);
                        String climate;
                        if (profile != null) {
                            climate = profile.summary;
                        } else {
                            climate = "CLIMA DESCONOCIDO: consultar pronostico real para la ciudad de " + city + ".";
                        }

                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent(climate);
                        reply.setConversationId(msg.getConversationId());
                        send(reply);

                        System.out.println(getLocalName() +
                                " respondio clima para " + city);
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
