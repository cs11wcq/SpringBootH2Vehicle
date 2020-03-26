package mitchell.SpringBootH2Vehicle;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;

//helpful constraints annotations: https://www.baeldung.com/java-bean-validation-not-null-empty-blank

@Entity //specifies that the class is an entity and mapped to a database table
public class Vehicle
{
    @Id //specifies the ID is the primary key of an entity
    @GeneratedValue //Provides for the specification of generation strategies for the values of primary keys.
    private int id;
    private int year;
    //@NotBlank means when Spring Boot validates the class instance,
    // the constrained fields must be not null and their trimmed length must be greater than zero
    @NotBlank(message = "Make may not be null nor empty")
    private String make;
    @NotBlank(message = "Model may not be null nor empty")
    private String model;

    public Vehicle()
    {

    }

    public Vehicle(int year, String make, String model)
    {
        this.year = year;
        this.make = make;
        this.model = model;
    }

    public int getId()
    {
        return id;
    }
    public void setId(int id)
    {
        this.id = id;
    }
    public int getYear()
    {
        return year;
    }
    public void setYear(int year)
    {
        this.year = year;
    }
    public String getMake()
    {
        return make;
    }
    public void setMake(String make)
    {
        this.make = make;
    }
    public String getModel()
    {
        return model;
    }
    public void setModel(String model)
    {
        this.model = model;
    }

    /**
     * Not required but useful for testing
     */
    public void printVehicleContents()
    {
        System.out.println("\tId: " + getId() + "\n" +
                "\tYear: " + getYear() + "\n" +
                "\tMake: " + getMake() + "\n" +
                "\tModel: " + getModel() + "\n");
    }

    /**
     * Compare that this vehicle equals another vehicle
     * @param v vehicle to compare to THIS vehicle
     * @return 1 if they have the same contents and 0 otherwise
     */
    public int containsSameContentsAs(Vehicle v)
    {
        if (v.getYear() == this.getYear()
                && v.getMake().equals(this.getMake()) && v.getModel().equals(this.getModel()))
        {
            return 1; //they are equal
        }
        else
            return 0;
    }
}

