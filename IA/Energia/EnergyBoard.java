package IA.Energia;

public class EnergyBoard {

    private int[] state;
    static private Clientes[] clientes;
    static private Centrales[] centrales;

    public EnergyBoard(int[] init, Clientes[] cli, Centrales[] centr){
        this.state = new int[init.length];
        this.clientes = cli.clone(); 

        for (int k = 0; k < init.length; ++k)
            this.state[k] = init[k]; 
    }

    public EnergyBoard getClone(){
        return (EnergyBoard) this.clone();
    }
}