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

public class BudgetAgent extends Agent {

    private static class BudgetProfile {
        final double basePerDay;
        final String costLevel; // referencia simple: bajo, medio, alto

        BudgetProfile(double basePerDay, String costLevel) {
            this.basePerDay = basePerDay;
            this.costLevel = costLevel;
        }
    }

    private static final Map<String, BudgetProfile> budgetByCity = new HashMap<>();

    static {
        budgetByCity.put("huaraz",   new BudgetProfile(220.0, "medio"));
        budgetByCity.put("lima",     new BudgetProfile(200.0, "medio"));
        budgetByCity.put("cusco",    new BudgetProfile(260.0, "medio-alto"));
        budgetByCity.put("arequipa", new BudgetProfile(230.0, "medio"));
        budgetByCity.put("iquitos",  new BudgetProfile(240.0, "medio"));
        budgetByCity.put("puno",     new BudgetProfile(210.0, "medio"));
        budgetByCity.put("paracas",  new BudgetProfile(250.0, "medio-alto"));
        budgetByCity.put("mancora",  new BudgetProfile(260.0, "medio-alto"));
        budgetByCity.put("trujillo", new BudgetProfile(210.0, "medio"));
        budgetByCity.put("nazca",    new BudgetProfile(220.0, "medio"));
    }

    @Override
    protected void setup() {
        System.out.println(getLocalName() + " (BudgetAgent) iniciado");
        registerService("budget-service", "budget-evaluator");

        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = myAgent.receive();
                if (msg != null) {
                    if (msg.getPerformative() == ACLMessage.REQUEST) {
                        String content = msg.getContent(); // ciudad;dias;presupuesto
                        String[] parts = content.split(";");
                        String city = parts[0];
                        int days = Integer.parseInt(parts[1]);
                        double budget = Double.parseDouble(parts[2]);

                        String normalized = city.trim().toLowerCase();

                        BudgetProfile profile = budgetByCity.get(normalized);
                        double basePerDay;
                        String costLevel;

                        if (profile != null) {
                            basePerDay = profile.basePerDay;
                            costLevel = profile.costLevel;
                        } else {
                            basePerDay = 250.0; // valor generico
                            costLevel = "desconocido";
                        }

                        double estimated = days * basePerDay;

                        String evaluation;
                        if (budget >= estimated) {
                            evaluation = "PRESUPUESTO ADECUADO. Costo estimado: "
                                    + estimated + " soles. Nivel de costo: " + costLevel + ".";
                        } else if (budget >= estimated * 0.8) {
                            evaluation = "PRESUPUESTO AJUSTADO. Costo estimado: "
                                    + estimated + " soles. Nivel de costo: " + costLevel
                                    + ". Se recomienda optimizar gastos.";
                        } else {
                            evaluation = "PRESUPUESTO INSUFICIENTE. Costo estimado: "
                                    + estimated + " soles. Nivel de costo: " + costLevel
                                    + ". Se recomienda aumentar presupuesto o reducir dias.";
                        }

                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent(evaluation);
                        reply.setConversationId(msg.getConversationId());
                        send(reply);

                        System.out.println(getLocalName() +
                                " evaluo presupuesto para " + days +
                                " dias en " + city);
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
