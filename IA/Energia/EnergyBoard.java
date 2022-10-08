package IA.Energia;
import java.lang.Math;
import java.util.*;

public class EnergyBoard {

    static private int n_clientes = 5;
    static private int n_centrales = 3;

    private int[] state;
    private int[] clientsXcentral;
    static private Clientes clientes;
    static private Centrales centrales;
    private double benefici;

    /**
     * Construct a new EnergyBoard, given an optional initial state, the costumers and the power plants
     *
     * @exception ArithmeticException the length of the initial state array is different to the number of costumers.
     * @param init the initial state of the board. If init[i] = -1 that means costumer <i>i</i> won't have any power plant assigned.
     * @param cli the Clientes object which represents the array of costumers to serve.
     * @param centr the Centrales object which represents the array of power plants available to us.
     */
    public EnergyBoard(int[] init, Clientes cli, Centrales centr) throws Exception {

        if (init.length != n_clientes) throw new ArithmeticException("The length of the state array should equal the number of costumers");
        
        state = new int[init.length];
        clientes = cli;
        centrales = centr; 
        clientsXcentral = new int [n_centrales];
        Arrays.fill(clientsXcentral, 0);
        benefici = 0;

        for (int c = 0; c < init.length; ++c){
            state[c] = init[c];

            if (state[c] == -1 && clientes.get(c).getContrato() == Cliente.NOGARANTIZADO){
                benefici -= VEnergia.getTarifaClientePenalizacion(clientes.get(c).getTipo()) * clientes.get(c).getConsumo();

            } else if (state[c] != -1){
                if (clientes.get(c).getContrato() == Cliente.NOGARANTIZADO) benefici += clientes.get(c).getConsumo() * VEnergia.getTarifaClienteNoGarantizada(clientes.get(c).getTipo());
                else benefici += clientes.get(c).getConsumo() * VEnergia.getTarifaClienteGarantizada(clientes.get(c).getTipo());

                ++clientsXcentral[state[c]];
            } 
        }
            
        for (int k = 0; k < n_centrales; ++k){
            if (clientsXcentral[k] == 0){
                benefici -= VEnergia.getCosteParada(centrales.get(k).getTipo());
            } else {
                benefici += (VEnergia.getCosteMarcha(centrales.get(k).getTipo()) + centrales.get(k).getProduccion()*VEnergia.getCosteProduccionMW(centrales.get(k).getTipo()));
            }
        }
    }

    /**
     * Clone an EnergyBoard
     * 
     * This operation returns a clone of the EnergyBoard.
     * 
     * <b>Important: Do not use the inherited .clone() for this operation. It won't work. Use this method instead.</b>
     */
    public EnergyBoard getClone() throws Exception{
        return (EnergyBoard) this.clone();
    }

    public int getNClients(){
        return n_clientes;
    }

    public int getNCentrals(){
        return n_centrales;
    }


    public int[] getState(){
        return state.clone();
    }

    public static Clientes getClientes(){
        return (Clientes) clientes.clone();
    }

    public static Centrales getCentrales(){
        return (Centrales) centrales.clone();
    }

    /**
     * Assign (or deassign) a power plant to a costumer
     * 
     * In order to perform this function it is highly advised to first check if it can legally be
     * executed with the <i>canAssign</i> boolean function (see below).
     *
     * @param c Costumer ID (offset in the state array)
     * @param k Power plant ID (offset in the power plant <i>centrales</i> array)
     * 
     * <i>Note that, as always, if k == -1 then we want to remove any power plant currently assigned</i>
     */
    public void assign(int c, int k) throws Exception {
        // Asignar la central k (posiblemente k = -1 (quitar central) ) al cliente c 
        int ant = state[c];
        state[c] = k;

        if (k == -1){
            benefici -= clientes.get(c).getConsumo() * VEnergia.getTarifaClienteNoGarantizada(clientes.get(c).getTipo());
            benefici -= VEnergia.getTarifaClientePenalizacion(clientes.get(c).getTipo()) * clientes.get(c).getConsumo();
            
            if (--clientsXcentral[ant] == 0){
                benefici -= VEnergia.getCosteParada(centrales.get(ant).getTipo());
                benefici += (VEnergia.getCosteMarcha(centrales.get(ant).getTipo()) + centrales.get(ant).getProduccion()*VEnergia.getCosteProduccionMW(centrales.get(ant).getTipo()));
            }
        } 
        else if (ant == -1) {
            benefici += clientes.get(c).getConsumo() * VEnergia.getTarifaClienteNoGarantizada(clientes.get(c).getTipo());
            benefici += VEnergia.getTarifaClientePenalizacion(clientes.get(c).getTipo()) * clientes.get(c).getConsumo();

            if (++clientsXcentral[k] == 1){
                benefici += VEnergia.getCosteParada(centrales.get(k).getTipo());
                benefici -= (VEnergia.getCosteMarcha(centrales.get(k).getTipo()) + centrales.get(k).getProduccion()*VEnergia.getCosteProduccionMW(centrales.get(k).getTipo()));
            }

        } else {
            // El cliente puede estar garantizado o no
            if (--clientsXcentral[ant] == 0){
                benefici -= VEnergia.getCosteParada(centrales.get(ant).getTipo());
                benefici += (VEnergia.getCosteMarcha(centrales.get(ant).getTipo()) + centrales.get(ant).getProduccion()*VEnergia.getCosteProduccionMW(centrales.get(ant).getTipo()));
            }

            if (++clientsXcentral[k] == 1){
                benefici += VEnergia.getCosteParada(centrales.get(k).getTipo());
                benefici -= (VEnergia.getCosteMarcha(centrales.get(k).getTipo()) + centrales.get(k).getProduccion()*VEnergia.getCosteProduccionMW(centrales.get(k).getTipo()));
            }

        }
    }

    /**
     * Check if we can assign a power plant to a costumer
     * 
     * The function will return <b>true</b> unless:
     *
     * - The costumer is GARANTIZADO and we will deassign a power plant
     * - The new power plant won't have enough wattage to satisfy all demand
     * - The current costumer has no power plant assign and we will try to deassign a plant
     * - The costumer already has assigned the plant we want to assign
     * 
     * @param c Costumer ID (offset in the state array)
     * @param k Power plant ID (offset in the power plant <i>centrales</i> array)
     *
     * @return A boolean whether we can assign the power plant to the costumer legally
     *
     * <i>Note that, as alwats, if k == -1 then we want to remove any power plant currently assigned</i>
     */
    public boolean canAssign(int c, int k){
        if (clientes.get(c).getContrato() == Cliente.GARANTIZADO && k == -1) return false;

        if (state[c] == k) return false;
        if (state[c] != -1 && k == -1) return true;

        double energyleft = energyLeft(k);

        if (energyleft < clientes.get(c).getConsumo()*(1. + VEnergia.getPerdida(getDistance(c, k))))
            return false;
        
        return true;
    }


    /**
     * Calculate the amount of wattage left for a power plant
     * 
     * The function will return a double representing the amount of wattage the power plant has left to serve.
     *
     * @param k Power plant ID (offset in the power plant <i>centrales</i> array) 
     *
     * @return A double representing the amount of energy, in Watts, the power plant has left
     */
    public double energyLeft(int k){

        double eIni = centrales.get(k).getProduccion();
        
        for (int c = 0; c < n_clientes; ++c){
            if (state[c] != k) continue;

            eIni -= (clientes.get(c).getConsumo()*(1. + VEnergia.getPerdida(getDistance(c, k))));
        }

        return eIni;
    }

    /**
     * Calculate the distance from a costumer to a power plant
     * 
     * This function will return a double representing the distance, in Kilometers, from a costumer to a power plant
     *
     * @param c Costumer ID (offset in the state array)
     * @param k Power plant ID (offset in the power plant <i>centrales</i> array)
     * 
     * @return A double representing the distance in Km. from costumer <i>c</i> to power plant <i>k</i>
     */
    public double getDistance(int c, int k){
        double cx = clientes.get(c).getCoordX();
        double cy = clientes.get(c).getCoordY();
        double kx = centrales.get(k).getCoordX();
        double ky = centrales.get(k).getCoordY();

        return Math.sqrt((cx-kx)*(cx-kx) + (cy-ky)*(cy-ky));
    }

    public boolean canSwap(int c1, int c2){
        if (state[c1] == state[c2]) return false;
        
        int k1 = state[c1]; 
        int k2 = state[c2];

        if (k1 == -1 && clientes.get(c2).getContrato() == Cliente.GARANTIZADO) return false;
        if (k2 == -1 && clientes.get(c1).getContrato() == Cliente.GARANTIZADO) return false;

        if (k1 != -1){
            double energyleft1 = energyLeft(k1) - clientes.get(c1).getConsumo()*(1. + VEnergia.getPerdida(getDistance(c1, k1)));
            if (energyleft1 < clientes.get(c2).getConsumo()*(1. + VEnergia.getPerdida(getDistance(c2, k1))))
                return false;
        }

        if (k2 != -1){
            double energyleft2 = energyLeft(k2) - clientes.get(c2).getConsumo()*(1. + VEnergia.getPerdida(getDistance(c2, k2)));
            if (energyleft2 < clientes.get(c1).getConsumo()*(1. + VEnergia.getPerdida(getDistance(c1, k2))))
                return false;
        }

        return true;
    }


    public void swap(int c1, int c2) throws Exception {
        // El client c1 passarà a tenir la central de c2 i el client c2 passarà a tenir la central del client c1
        // És possible que c1 o c2 no tinguin central, pero almenys una n'ha de tenir
        // central de c1 != central de c2,   s'han de seguir complint totes les restriccions de les solucions

        int k1 = state[c1];
        int k2 = state[c2];
        state[c1] = k2;
        state[c2] = k1;

        if (k1 != -1 && k2 != -1) return;

        if (k1 == -1){
            benefici += clientes.get(c1).getConsumo() * VEnergia.getTarifaClienteNoGarantizada(clientes.get(c1).getTipo());
            benefici += VEnergia.getTarifaClientePenalizacion(clientes.get(c1).getTipo()) * clientes.get(c1).getConsumo();

            benefici -= clientes.get(c2).getConsumo() * VEnergia.getTarifaClienteNoGarantizada(clientes.get(c2).getTipo());
            benefici -= VEnergia.getTarifaClientePenalizacion(clientes.get(c2).getTipo()) * clientes.get(c2).getConsumo();
            return;
        }

        if (k2 == -1){
            benefici -= clientes.get(c1).getConsumo() * VEnergia.getTarifaClienteNoGarantizada(clientes.get(c1).getTipo());
            benefici -= VEnergia.getTarifaClientePenalizacion(clientes.get(c1).getTipo()) * clientes.get(c1).getConsumo();

            benefici += clientes.get(c2).getConsumo() * VEnergia.getTarifaClienteNoGarantizada(clientes.get(c2).getTipo());
            benefici += VEnergia.getTarifaClientePenalizacion(clientes.get(c2).getTipo()) * clientes.get(c2).getConsumo();
            return;
        }

    }

    
    public double getHeuristic(){
        return -benefici;
    }

    //Genera solució inicial on cada client garantit està assignat a una central de manera aleatòria. Els clients garantitzats no estan assignats tenen marcats central -1.
    public void initialState(Random rnd) throws Exception { 
        for (int i = 0; i < n_clientes; ++i) {

            if (clientes.get(i).getContrato() == Cliente.GARANTIZADO){
                state[i] = randomCentral(i, rnd);

                System.out.println("\n Assigned central "+state[i]+" to client "+i+"\n");

                if (++clientsXcentral[state[i]] == 1){
                    benefici += VEnergia.getCosteParada(centrales.get(state[i]).getTipo());
                    benefici -= (VEnergia.getCosteMarcha(centrales.get(state[i]).getTipo()) + centrales.get(state[i]).getProduccion()*VEnergia.getCosteProduccionMW(centrales.get(state[i]).getTipo()));
                }

                benefici += clientes.get(i).getConsumo() * VEnergia.getTarifaClienteGarantizada(clientes.get(i).getTipo());
            }

            else benefici -= VEnergia.getTarifaClientePenalizacion(clientes.get(i).getTipo()) * clientes.get(i).getConsumo();
        }
    }
    //Genera solució inicial on cada client està assignat a una central de manera aleatòria, els clients no garantitzats tenen possibilitats de no estar assignades a cap
    public void initialState2(Random rnd) throws Exception {
        for (int i = 0; i < n_clientes; ++i) {
            if (clientes.get(i).getContrato() == Cliente.GARANTIZADO || rnd.nextInt()%2 == 0){
                state[i] = randomCentral(i, rnd);

                if (++clientsXcentral[state[i]] == 1){
                    benefici += VEnergia.getCosteParada(centrales.get(state[i]).getTipo());
                    benefici -= (VEnergia.getCosteMarcha(centrales.get(state[i]).getTipo()) + centrales.get(state[i]).getProduccion()*VEnergia.getCosteProduccionMW(centrales.get(state[i]).getTipo()));
                }

                if (clientes.get(i).getContrato() == Cliente.GARANTIZADO) benefici += clientes.get(i).getConsumo() * VEnergia.getTarifaClienteGarantizada(clientes.get(i).getTipo());
                else benefici += clientes.get(i).getConsumo() * VEnergia.getTarifaClienteNoGarantizada(clientes.get(i).getTipo());

            } else benefici -= VEnergia.getTarifaClientePenalizacion(clientes.get(i).getTipo()) * clientes.get(i).getConsumo();
        }
    }

    //Retorna una central aleatoria on el client es pot assignar
    private int randomCentral(int c, Random rnd) {
        int k = rnd.nextInt()%n_centrales;
        if (k < 0) k = -k;

        while (!canAssign(c, k)) {
            k = rnd.nextInt()%n_centrales;
            if (k < 0) k = -k;
        }
        
        return k;
    }

    public String toString(){
        String s = "\nCENTRALES\n";
        // Where centrals are located
        for (int k = 0; k < n_centrales; ++k){
            s += "Central "+k+" (";
            s += centrales.get(k).getCoordX()+", "+centrales.get(k).getCoordY()+")\n";
        }

        s += "\nCLIENTES\n";

        for (int c = 0; c < n_clientes; ++c){
            s += "Cliente "+c+" (";
            s += clientes.get(c).getCoordX()+", "+clientes.get(c).getCoordY()+")";
            s += " tiene central " + state[c] + " y es cliente ";

            s += "TIPO " + clientes.get(c).getTipo() + " CONTRATO " + clientes.get(c).getContrato();

            s += "\n";
        }

        return s;
    }
}