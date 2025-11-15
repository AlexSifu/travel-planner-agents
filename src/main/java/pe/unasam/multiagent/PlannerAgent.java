package pe.unasam.multiagent;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class PlannerAgent extends Agent {

    private String city;
    private int days;
    private double budget;

    private String weatherInfo;
    private String budgetInfo;
    private String activityInfo;

    @Override
    protected void setup() {

        // Leer argumentos enviados desde MainContainer
        Object[] args = getArguments();
        if (args != null && args.length >= 3) {
            city = (String) args[0];
            days = Integer.parseInt(args[1].toString());
            budget = Double.parseDouble(args[2].toString());
        } else {
            // Valores por defecto por si acaso
            city = "Cusco";
            days = 4;
            budget = 1500.0;
        }

        System.out.println(getLocalName() + " iniciado. Ciudad=" + city
                + ", dias=" + days + ", presupuesto=" + budget);

        // Registrar el planner en las paginas amarillas (no es obligatorio, pero es mas completo)
        registerService("planner-service", "travel-planner");

        // Enviar solicitudes a los otros agentes
        sendRequests();

        // Comportamiento para recibir resultados de los otros agentes
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                MessageTemplate mt =
                        MessageTemplate.MatchConversationId("travel-planning");
                ACLMessage msg = myAgent.receive(mt);

                if (msg != null) {

                    String sender = msg.getSender().getLocalName();

                    if (sender.startsWith("Weather")) {
                        weatherInfo = msg.getContent();
                    } else if (sender.startsWith("Budget")) {
                        budgetInfo = msg.getContent();
                    } else if (sender.startsWith("Activity")) {
                        activityInfo = msg.getContent();
                    }

                    // Cuando ya tenemos las 3 respuestas, construimos la recomendacion final
                    if (weatherInfo != null && budgetInfo != null && activityInfo != null) {

                        System.out.println("===== RECOMENDACION FINAL =====");
                        System.out.println("Ciudad: " + city +
                                " / Dias: " + days +
                                " / Presupuesto: " + budget);
                        System.out.println("Clima estimado: " + weatherInfo);
                        System.out.println("Evaluacion de presupuesto: " + budgetInfo);
                        System.out.println("Actividades sugeridas: " + activityInfo);

                        String decision;

                        boolean climaBueno =
                                weatherInfo.toUpperCase().contains("BUENO")
                                        || weatherInfo.toUpperCase().contains("SECO")
                                        || weatherInfo.toUpperCase().contains("SOLEADO");

                        boolean climaComplicado =
                                weatherInfo.toUpperCase().contains("LLUVIOSO")
                                        || weatherInfo.toUpperCase().contains("HUMEDO")
                                        || weatherInfo.toUpperCase().contains("RIESGO");

                        boolean presupuestoAdecuado =
                                budgetInfo.toUpperCase().contains("ADECUADO");

                        boolean presupuestoAjustado =
                                budgetInfo.toUpperCase().contains("AJUSTADO");

                        // Logica de decision simple pero clara
                        if (climaBueno && presupuestoAdecuado) {
                            decision = "RECOMENDADO realizar el viaje.";
                        } else if (!climaComplicado && (presupuestoAdecuado || presupuestoAjustado)) {
                            decision = "Viaje posible, pero revisar detalles de clima y gastos.";
                        } else {
                            decision = "NO recomendado en este momento.";
                        }

                        System.out.println("Decision: " + decision);
                        System.out.println("================================");
                        System.out.println();

                        // Reinicio por si mas adelante quieres permitir nuevas consultas
                        weatherInfo = null;
                        budgetInfo = null;
                        activityInfo = null;
                    }

                } else {
                    block();
                }
            }
        });
    }

    private void sendRequests() {

        // WeatherAgent
        AID weatherAID = searchServiceAgent("weather-service");
        if (weatherAID != null) {
            ACLMessage reqWeather = new ACLMessage(ACLMessage.REQUEST);
            reqWeather.addReceiver(weatherAID);
            reqWeather.setConversationId("travel-planning");
            reqWeather.setContent(city + ";" + days);
            send(reqWeather);
        } else {
            System.out.println("No se encontro weather-service en las paginas amarillas.");
        }

        // BudgetAgent
        AID budgetAID = searchServiceAgent("budget-service");
        if (budgetAID != null) {
            ACLMessage reqBudget = new ACLMessage(ACLMessage.REQUEST);
            reqBudget.addReceiver(budgetAID);
            reqBudget.setConversationId("travel-planning");
            reqBudget.setContent(city + ";" + days + ";" + budget);
            send(reqBudget);
        } else {
            System.out.println("No se encontro budget-service en las paginas amarillas.");
        }

        // ActivityAgent (nuevo)
        AID activityAID = searchServiceAgent("activity-service");
        if (activityAID != null) {
            ACLMessage reqActivity = new ACLMessage(ACLMessage.REQUEST);
            reqActivity.addReceiver(activityAID);
            reqActivity.setConversationId("travel-planning");
            reqActivity.setContent(city + ";" + days);
            send(reqActivity);
        } else {
            System.out.println("No se encontro activity-service en las paginas amarillas.");
        }
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

    private AID searchServiceAgent(String serviceType) {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(serviceType);
        template.addServices(sd);

        try {
            DFAgentDescription[] result = DFService.search(this, template);
            if (result != null && result.length > 0) {
                return result[0].getName();
            }
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        return null;
    }
}
