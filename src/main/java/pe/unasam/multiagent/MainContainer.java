package pe.unasam.multiagent;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.leap.Properties;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

import java.util.Scanner;

public class MainContainer {

    public static void main(String[] args) {
        try {
            // Pedir datos al usuario por consola
            Scanner scanner = new Scanner(System.in);

            System.out.println("=== Planificador de viaje multi agente ===");
            System.out.print("Ciudad destino (ejemplo: Cusco, Huaraz, Lima): ");
            String city = scanner.nextLine();

            System.out.print("Cantidad de dias: ");
            int days = Integer.parseInt(scanner.nextLine());

            System.out.print("Presupuesto total en soles: ");
            double budget = Double.parseDouble(scanner.nextLine());

            // Configurar JADE con GUI
            Properties pp = new Properties();
            pp.setProperty(Profile.GUI, "true");
            Profile p = new ProfileImpl(pp);

            Runtime rt = Runtime.instance();
            AgentContainer mainContainer = rt.createMainContainer(p);

            // Argumentos para el PlannerAgent
            Object[] plannerArgs = new Object[] { city, days, budget };

            AgentController planner = mainContainer.createNewAgent(
                    "Planner",
                    "pe.unasam.multiagent.PlannerAgent",
                    plannerArgs);

            AgentController weather = mainContainer.createNewAgent(
                    "WeatherService",
                    "pe.unasam.multiagent.WeatherAgent",
                    null);

            AgentController budgetAgent = mainContainer.createNewAgent(
                    "BudgetService",
                    "pe.unasam.multiagent.BudgetAgent",
                    null);

            AgentController activity = mainContainer.createNewAgent(
                    "ActivityService",
                    "pe.unasam.multiagent.ActivityAgent",
                    null);

            planner.start();
            weather.start();
            budgetAgent.start();
            activity.start();

            System.out.println("Contenedor principal y agentes iniciados.");

        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}
