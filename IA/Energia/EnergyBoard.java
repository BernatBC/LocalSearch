package IA.Energia;
import java.lang.Math;
import java.util.*;

public class EnergyBoard {

    static private int n_clientes;
    static private int n_centrales;

    private int[] state;
    private int[] clientsXcentral;
    static private Clientes clientes;
    static private Centrales centrales;
    private double benefici;
    private double distance;
    private double energy;
    private double[]  energyleft;
    static private int[][] closestCentrals;
    private int nonAssignedG;
    
    
	public EnergyBoard(int[] init, double ben, double dis, double ene, double[] eleft, int[] cXk, int nAG){
		state = init.clone();
		energyleft = eleft.clone();
		clientsXcentral = cXk.clone();
		benefici = ben;
		distance = dis;
		energy = ene;
		nonAssignedG = nAG;
	}
    
    
    /**
     * Construct a new EnergyBoard, given an optional initial state, the costumers and the power plants
     *
     * @exception ArithmeticException the length of the initial state array is different to the number of costumers.
     * @param init the initial state of the board. If init[i] = -1 that means costumer <i>i</i> won't have any power plant assigned.
     * @param cli the Clientes object which represents the array of costumers to serve.
     * @param centr the Centrales object which represents the array of power plants available to us.
     */
    public EnergyBoard(int[] init, Clientes cli, Centrales centr) throws Exception {

        if (init.length != cli.toArray().length) throw new ArithmeticException("The length of the state array should equal the number of costumers");
        
        // Inicialitzem els nombres de clients i nombres de centrals
        n_clientes = init.length;
        n_centrales = centr.toArray().length;

        // L'estat, representat per una array on state[c] = k <--> El client 'c' està assignat a la central 'k' (o cap si k == -1)
        state = new int[init.length];
        clientes = cli;
        centrales = centr;
		nonAssignedG = 0;

        // Array que ens diu quant d'energia li queda a cada central
        energyleft = new double[n_centrales];
        Arrays.fill(energyleft, 0);

        // Matriu que ens diu per cada client l'ordre de les centrals per proximitat
        closestCentrals = new int[n_clientes][n_centrales];

        // Array que ens permet conèixer en tot moment quants clients es troben per cada central
        clientsXcentral = new int [n_centrales];
        Arrays.fill(clientsXcentral, 0);
        benefici = 0.0;
        distance = 0.0;
        energy   = 0.0;

        for (int c = 0; c < init.length; ++c){

            state[c] = init[c];

            distance += getDistance(c, state[c]);
            
            if (state[c] == -1 && clientes.get(c).getContrato() == Cliente.GARANTIZADO) ++nonAssignedG;

            if (state[c] == -1 && clientes.get(c).getContrato() == Cliente.NOGARANTIZADO){
                benefici -= VEnergia.getTarifaClientePenalizacion(clientes.get(c).getTipo()) * clientes.get(c).getConsumo();
                

            } else if (state[c] != -1){
                // Guany, hi ha un client consumint
                if (clientes.get(c).getContrato() == Cliente.NOGARANTIZADO) benefici += clientes.get(c).getConsumo() * VEnergia.getTarifaClienteNoGarantizada(clientes.get(c).getTipo());
                else benefici += clientes.get(c).getConsumo() * VEnergia.getTarifaClienteGarantizada(clientes.get(c).getTipo());

                energy -= (clientes.get(c).getConsumo()/(1. - VEnergia.getPerdida(getDistance(c, state[c]))));
                energyleft[state[c]] -= (clientes.get(c).getConsumo()/(1. - VEnergia.getPerdida(getDistance(c, state[c]))));

                ++clientsXcentral[state[c]];

            }
        }


        for (int k = 0; k < n_centrales; ++k){

            energyleft[k] += centrales.get(k).getProduccion();

            if (clientsXcentral[k] != 0) energy += centrales.get(k).getProduccion();

            if (clientsXcentral[k] == 0){
                benefici -= VEnergia.getCosteParada(centrales.get(k).getTipo());
            } else {
                benefici -= (VEnergia.getCosteMarcha(centrales.get(k).getTipo()) + centrales.get(k).getProduccion()*VEnergia.getCosteProduccionMW(centrales.get(k).getTipo()));
            }
        }

        //System.out.println("TOTAL ENERGY PROVIDED BY ALL POWER PLANTS = "+energyprovided);
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
    
    public int[] getcXk(){
		return clientsXcentral.clone();
	}
	
	public double getDistance(){
		return distance;
	}
	
	public int getNAG(){
		return nonAssignedG;
	}
	
	public double getEnergy(){
		return energy;
	}
	
	public double[] getEnergyLeft(){
		return energyleft.clone();
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

        distance -= getDistance(c, ant);
        distance += getDistance(c, k);

        if (k == -1){
            energy += (clientes.get(c).getConsumo()/(1. - VEnergia.getPerdida(getDistance(c, ant))));
            energyleft[ant] += (clientes.get(c).getConsumo()/(1. - VEnergia.getPerdida(getDistance(c, ant))));

            benefici -= clientes.get(c).getConsumo() * VEnergia.getTarifaClienteNoGarantizada(clientes.get(c).getTipo());
            benefici -= VEnergia.getTarifaClientePenalizacion(clientes.get(c).getTipo()) * clientes.get(c).getConsumo();
            
            if (--clientsXcentral[ant] == 0){
                energy -= centrales.get(ant).getProduccion();

                benefici -= VEnergia.getCosteParada(centrales.get(ant).getTipo());
                benefici += (VEnergia.getCosteMarcha(centrales.get(ant).getTipo()) + centrales.get(ant).getProduccion()*VEnergia.getCosteProduccionMW(centrales.get(ant).getTipo()));
            }
        } 
        else if (ant == -1) {
            if (clientes.get(c).getContrato() == Cliente.GARANTIZADO) --nonAssignedG;

            energy -= (clientes.get(c).getConsumo()/(1. - VEnergia.getPerdida(getDistance(c, k))));
            energyleft[k] -= (clientes.get(c).getConsumo()/(1. - VEnergia.getPerdida(getDistance(c, k))));

            benefici += clientes.get(c).getConsumo() * VEnergia.getTarifaClienteNoGarantizada(clientes.get(c).getTipo());
            benefici += VEnergia.getTarifaClientePenalizacion(clientes.get(c).getTipo()) * clientes.get(c).getConsumo();

            if (++clientsXcentral[k] == 1){
                //System.out.println("Central "+k+" puesta en marcha!\n");
                energy += centrales.get(k).getProduccion();

                benefici += VEnergia.getCosteParada(centrales.get(k).getTipo());
                benefici -= (VEnergia.getCosteMarcha(centrales.get(k).getTipo()) + centrales.get(k).getProduccion()*VEnergia.getCosteProduccionMW(centrales.get(k).getTipo()));
            }

        } else {
            energy += (clientes.get(c).getConsumo()/(1. - VEnergia.getPerdida(getDistance(c, ant))));
            energy -= (clientes.get(c).getConsumo()/(1. - VEnergia.getPerdida(getDistance(c, k))));

            energyleft[ant] += (clientes.get(c).getConsumo()/(1. - VEnergia.getPerdida(getDistance(c, ant))));
            energyleft[k]   -= (clientes.get(c).getConsumo()/(1. - VEnergia.getPerdida(getDistance(c, k))));
            // El cliente puede estar garantizado o no
            if (--clientsXcentral[ant] == 0){
                energy -= centrales.get(ant).getProduccion();

                benefici -= VEnergia.getCosteParada(centrales.get(ant).getTipo());
                benefici += (VEnergia.getCosteMarcha(centrales.get(ant).getTipo()) + centrales.get(ant).getProduccion()*VEnergia.getCosteProduccionMW(centrales.get(ant).getTipo()));
            }

            if (++clientsXcentral[k] == 1){
                energy += centrales.get(k).getProduccion();

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

        if (energyleft[k] < clientes.get(c).getConsumo()/(1. - VEnergia.getPerdida(getDistance(c, k))))
            return false;
        
        return true;
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
        if (k == -1) return 140.0; // Penalització per no tenir central

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
            double energyleft1 = energyleft[k1] + clientes.get(c1).getConsumo()/(1. - VEnergia.getPerdida(getDistance(c1, k1)));
            if (energyleft1 < clientes.get(c2).getConsumo()/(1. - VEnergia.getPerdida(getDistance(c2, k1))))
                return false;
        }

        if (k2 != -1){
            double energyleft2 = energyleft[k2] + clientes.get(c2).getConsumo()/(1. - VEnergia.getPerdida(getDistance(c2, k2)));
            if (energyleft2 < clientes.get(c1).getConsumo()/(1. - VEnergia.getPerdida(getDistance(c1, k2))))
                return false;
        }

        return true;
    }


    public void swap(int c1, int c2) throws Exception {
        // El client c1 passarà a tenir la central de c2 i el client c2 passarà a tenir la central del client c1
        // És possible que c1 o c2 no tinguin central, pero almenys una n'ha de tenir
        // central de c1 != central de c2, s'han de seguir complint totes les restriccions de les solucions

        int k1 = state[c1];
        int k2 = state[c2];
        state[c1] = k2;
        state[c2] = k1;

        distance -= getDistance(c1, k1);
        distance -= getDistance(c2, k2);
        distance += getDistance(c2, k1);
        distance += getDistance(c1, k2);

        if (k1 != -1 && k2 != -1){
            energy += (clientes.get(c1).getConsumo()/(1. - VEnergia.getPerdida(getDistance(c1, k1))));
            energyleft[k1] += (clientes.get(c1).getConsumo()/(1. - VEnergia.getPerdida(getDistance(c1, k1))));
            energy -= (clientes.get(c2).getConsumo()/(1. - VEnergia.getPerdida(getDistance(c2, k1))));
            energyleft[k1] -= (clientes.get(c2).getConsumo()/(1. - VEnergia.getPerdida(getDistance(c2, k1))));
            energy += (clientes.get(c2).getConsumo()/(1. - VEnergia.getPerdida(getDistance(c2, k2))));
            energyleft[k2] += (clientes.get(c2).getConsumo()/(1. - VEnergia.getPerdida(getDistance(c2, k2))));
            energy -= (clientes.get(c1).getConsumo()/(1. - VEnergia.getPerdida(getDistance(c1, k2))));
            energyleft[k2] -= (clientes.get(c1).getConsumo()/(1. - VEnergia.getPerdida(getDistance(c1, k2))));

        }

        else if (k1 == -1){
            if (clientes.get(c1).getContrato() == Cliente.GARANTIZADO) --nonAssignedG;

            energy += (clientes.get(c2).getConsumo()/(1. - VEnergia.getPerdida(getDistance(c2, k2))));
            energyleft[k2] += (clientes.get(c2).getConsumo()/(1. - VEnergia.getPerdida(getDistance(c2, k2))));
            energy -= (clientes.get(c1).getConsumo()/(1. - VEnergia.getPerdida(getDistance(c1, k2))));
            energyleft[k2] -= (clientes.get(c1).getConsumo()/(1. - VEnergia.getPerdida(getDistance(c1, k2))));

            benefici += clientes.get(c1).getConsumo() * VEnergia.getTarifaClienteNoGarantizada(clientes.get(c1).getTipo());
            benefici += VEnergia.getTarifaClientePenalizacion(clientes.get(c1).getTipo()) * clientes.get(c1).getConsumo();

            benefici -= clientes.get(c2).getConsumo() * VEnergia.getTarifaClienteNoGarantizada(clientes.get(c2).getTipo());
            benefici -= VEnergia.getTarifaClientePenalizacion(clientes.get(c2).getTipo()) * clientes.get(c2).getConsumo();
        }

        else {
            if (clientes.get(c2).getContrato() == Cliente.GARANTIZADO) --nonAssignedG;
            // k2 == -1
            energy += (clientes.get(c1).getConsumo()/(1. - VEnergia.getPerdida(getDistance(c1, k1))));
            energyleft[k1] += (clientes.get(c1).getConsumo()/(1. - VEnergia.getPerdida(getDistance(c1, k1))));
            energy -= (clientes.get(c2).getConsumo()/(1. - VEnergia.getPerdida(getDistance(c2, k1))));
            energyleft[k1] -= (clientes.get(c2).getConsumo()/(1. - VEnergia.getPerdida(getDistance(c2, k1))));

            benefici -= clientes.get(c1).getConsumo() * VEnergia.getTarifaClienteNoGarantizada(clientes.get(c1).getTipo());
            benefici -= VEnergia.getTarifaClientePenalizacion(clientes.get(c1).getTipo()) * clientes.get(c1).getConsumo();

            benefici += clientes.get(c2).getConsumo() * VEnergia.getTarifaClienteNoGarantizada(clientes.get(c2).getTipo());
            benefici += VEnergia.getTarifaClientePenalizacion(clientes.get(c2).getTipo()) * clientes.get(c2).getConsumo();
        }

    }

    
    public double getHeuristic(){

        return (distance*Math.log(distance) + energy)*(nonAssignedG + 1);
    }

    public double getBenefici(){
        return benefici;
    }

    public void mergesort(int c, int l, int r, int[] arr){
        if (l < r){
            int m = (l+r) / 2;
            mergesort(c, l, m, arr);
            mergesort(c, m+1, r, arr);
            merge(c, l, r, m, arr);
        }

    }

    public void merge(int c, int l, int r, int m, int[] arr){
        int i, j, k;

        int[] B = new int[arr.length];

        for (i=l; i<=r; ++i)
            B[i] = arr[i];

        i = l; j = m + 1; k = l;

        while (i <= m && j <= r){
            if (getDistance(c, i) <= getDistance(c, j))
                arr[k++] = B[i++];
            else 
                arr[k++] = B[j++];
        }                

        while (i<=m)
            arr[k++] = B[i++];
    }

    //Genera solució inicial on cada client garantit està assignat a una central de manera aleatòria. Els clients garantitzats no estan assignats tenen marcats central -1.
    public void initialState(Random rnd) throws Exception { 

        for (int i = 0; i < n_clientes; ++i) {

            for (int k = 0; k < n_centrales; ++k) closestCentrals[i][k] = k;

            // Fem el sort
            mergesort(i, 0, n_centrales - 1, closestCentrals[i]);

            if (clientes.get(i).getContrato() == Cliente.GARANTIZADO){
                
                distance -= getDistance(i, state[i]);

                int k = 0; 
                while (!canAssign(i, closestCentrals[i][k])){
                    ++k;
                    if (k >= n_centrales) System.out.println("STUCK STUCK SUCK");
                }

                state[i] = closestCentrals[i][k];
                //System.out.println("ASSIGN central "+closestCentrals[i][k]+" to client "+i);

                distance += getDistance(i, state[i]);
                
                energyleft[state[i]] -= (clientes.get(i).getConsumo()/(1. - VEnergia.getPerdida(getDistance(i, state[i]))));
                energy -= (clientes.get(i).getConsumo()/(1. - VEnergia.getPerdida(getDistance(i, state[i]))));

                if (++clientsXcentral[state[i]] == 1){
                    energy += centrales.get(state[i]).getProduccion();

                    benefici += VEnergia.getCosteParada(centrales.get(state[i]).getTipo());
                    benefici -= (VEnergia.getCosteMarcha(centrales.get(state[i]).getTipo()) + centrales.get(state[i]).getProduccion()*VEnergia.getCosteProduccionMW(centrales.get(state[i]).getTipo()));
                }

                benefici += clientes.get(i).getConsumo() * VEnergia.getTarifaClienteGarantizada(clientes.get(i).getTipo());
            }

        }
    }
    //Genera solució inicial on cada client està assignat a una central de manera aleatòria, els clients no garantitzats tenen possibilitats de no estar assignades a cap
    public void initialState2(Random rnd) throws Exception {
        for (int i = 0; i < n_clientes; ++i) {
			if (clientes.get(i).getContrato() == Cliente.NOGARANTIZADO) continue;
			
            distance -= getDistance(i, state[i]);

            state[i] = randomCentral(i, rnd);

            distance += getDistance(i, state[i]);
				
            if (state[i] == -1) continue;
            else if (state[i] != -1) --nonAssignedG;
            
            energy -= (clientes.get(i).getConsumo()/(1. - VEnergia.getPerdida(getDistance(i, state[i]))));
            energyleft[state[i]] -= (clientes.get(i).getConsumo()/(1. - VEnergia.getPerdida(getDistance(i, state[i]))));

            if (++clientsXcentral[state[i]] == 1){
                energy += centrales.get(state[i]).getProduccion();
                
                benefici += VEnergia.getCosteParada(centrales.get(state[i]).getTipo());
                benefici -= (VEnergia.getCosteMarcha(centrales.get(state[i]).getTipo()) + centrales.get(state[i]).getProduccion()*VEnergia.getCosteProduccionMW(centrales.get(state[i]).getTipo()));
            }

            benefici += clientes.get(i).getConsumo() * VEnergia.getTarifaClienteGarantizada(clientes.get(i).getTipo());

        }
    }
    
    private int randomCentral(int c, Random rnd) {
        int k = rnd.nextInt(n_centrales);
        int org = k;

        while (!canAssign(c, k)) {
			k = (k + 1) % n_centrales;
			if (k == org) return -1;
        }

        
        return k;
    }

    public String toString(){
        String s = "\nCENTRALES\n";
        // Where centrals are located
        for (int k = 0; k < n_centrales; ++k){
            s += "K "+k+" (";
            s += centrales.get(k).getCoordX()+", "+centrales.get(k).getCoordY()+")";
            s += " produce " + centrales.get(k).getProduccion() + "W ";

            try {
                s += " PARADA = " + (int) VEnergia.getCosteParada(centrales.get(k).getTipo()) + " MARCHA = ";
                s += (int) VEnergia.getCosteMarcha(centrales.get(k).getTipo()) + centrales.get(k).getProduccion()*VEnergia.getCosteProduccionMW(centrales.get(k).getTipo());
                s += " TIPO " + centrales.get(k).getTipo() + "\n";
            } catch (Exception e){
                System.out.println(e);
            }
        }

        s += "\nCLIENTES\n";

        for (int c = 0; c < n_clientes; ++c){
            s += "C "+c+" (";
            s += clientes.get(c).getCoordX()+", "+clientes.get(c).getCoordY()+")";
            s += " tiene K " + state[c] + " y es ";

            if (clientes.get(c).getContrato() == Cliente.GARANTIZADO) s += " Gar. ";
            else s += " No G.";

            s += " DE TIPO " + clientes.get(c).getTipo();
            s += " CONSUME " + clientes.get(c).getConsumo()+"W";
            if (state[c] != -1) s += " Y QUITA " + clientes.get(c).getConsumo()/(1. - VEnergia.getPerdida(getDistance(c, state[c])))+"W";

            s += "\n";
        }

        return s;
    }
}
