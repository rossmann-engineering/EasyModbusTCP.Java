package de.re.easymodbus.modbusserver;


class DataModel
{
    private int[] holdingRegisters = new int[65535];
    private int[] inputRegisters = new int[65535];
    private boolean[] coils = new boolean[65535];
    private boolean[] discreteInputs = new boolean[65535];
    
    private int[] mqttHoldingRegistersOldValues = new int[65535];
    private int[] mqttInputRegistersOldValues = new int[65535];
    private boolean[] mqttCoilsOldValues = new boolean[65535];
    private boolean[] mqttDiscreteInputsOldValues = new boolean[65535];
    
    public void setHoldingRegister(int i, int value)
    {
    	holdingRegisters[i] = value;

    }
    
    public void setInputRegister(int i, int value)
    {
    	inputRegisters[i] = value;

    }
    
    public void setCoil(int i, boolean value)
    {
    	coils[i] = value;

    }
    
    public void setDiscreteInput(int i, boolean value)
    {
    	discreteInputs[i] = value;

    }
    
    public int getHoldingRegister(int i)
    {
    	return holdingRegisters[i];
    }
    
    public int getInputRegister(int i)
    {
    	return inputRegisters[i];
    }
    
    public boolean getCoil(int i)
    {
    	return coils[i];
    }
    
    public boolean getDiscreteInput(int i)
    {
    	return discreteInputs[i];
    }
   
}
