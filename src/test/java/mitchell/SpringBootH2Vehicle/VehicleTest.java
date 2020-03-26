package mitchell.SpringBootH2Vehicle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class VehicleTest
{
    @Test
    public void testVehicleInitialized()
    {
        Vehicle vehicle = new Vehicle();
        assertEquals(vehicle.getYear(), 0);
    }

    @Test
    public void testVehicleSetProperties()
    {
        Vehicle vehicle = new Vehicle();

        vehicle.setId(1);
        vehicle.setYear(2000);
        vehicle.setMake("Toyota");
        vehicle.setModel("Prius");

        vehicle.printVehicleContents();
    }

    @Test
    public void testVehicleGetProperties()
    {
        Vehicle vehicle = new Vehicle();
        Vehicle vehicle1 = new Vehicle(2010, "chevy", "bolt");

        vehicle.setId(1);
        vehicle.setYear(2000);
        vehicle.setMake("Toyota");
        vehicle.setModel("Prius");

        assertEquals(vehicle.getId(), 1);
        assertEquals(vehicle.getYear(), 2000);
        assertEquals(vehicle.getMake(), "Toyota");
        assertNotEquals(vehicle.getMake(), "T");
        assertEquals(vehicle.getModel(), "Prius");

        assertEquals(vehicle1.getId(), 0); //test id is initialized to zero
    }

}
