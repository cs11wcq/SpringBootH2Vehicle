package mitchell.SpringBootH2Vehicle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Seth Kim
 * 2/14/20 Valentine's Day
 * For Mitchell International
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class VehicleControllerTest
{
    @Autowired
    //inject a rest template which allows us to make calls to our API
    private TestRestTemplate restTemplate;
    @LocalServerPort
    private int port; //port gets assigned a random port
    //localhost:8080/persons/
    private static final String url = "/vehicles/";

    private String getUrl()
    {
        return "http://localhost:" + port + url;
    }
    private void delete(int id)
    {
        System.out.println("DElete id is " + id);
        restTemplate.delete(getUrl()+id);
    }

    /**
     * Clean the database before each Test method
     */
    @BeforeEach
    public void beforeEachTest()
    {
        ResponseEntity<Vehicle[]> boop = restTemplate.getForEntity(getUrl(), Vehicle[].class);
        for (Vehicle v: boop.getBody())
        {
            delete(v.getId());
        }
        System.out.println("HELLO\n");
//        assertEquals(size, 0);
    }
    @Test
    public void testDataBaseEmptyInitially ()
    {
        ResponseEntity<String> response = restTemplate.getForEntity(getUrl(), String.class);
        assertEquals(response.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void testCreateOneVehicle() throws Exception
    {
        //prepare
        Vehicle vehicle = new Vehicle(2000, "Honda", "Odyssey");
        //execute
        ResponseEntity<Vehicle> responseEntity = restTemplate.postForEntity(getUrl(), vehicle, Vehicle.class);
        assertNotNull(responseEntity);
        //collect response
        Vehicle resultVehicle = responseEntity.getBody(); //vehicle from database
        int status = responseEntity.getStatusCodeValue();
        //verify
        assertEquals(HttpStatus.OK.value(), status);
        assertNotNull(resultVehicle);
        assertEquals(resultVehicle.getYear(), vehicle.getYear());
        assertEquals(resultVehicle.getMake(), vehicle.getMake());
        assertEquals(resultVehicle.getModel(), vehicle.getModel());
//        resultVehicle.printVehicleContents();
    }

    //https://www.baeldung.com/junit-assert-exception\

    /**
     * test that when trying to create a Vehicle with a year that is not within 1950-2050, an
     * InvalidYearException is thrown. If it is not thrown, then this method should fail
     */
    //TODO
    @Test //(expected = InvalidYearException.class)
    public void testInvalidYearExceptionThrownWhenCreatingVehicleWithYearNotInRange()
    {
        //prepare
        Vehicle vehicle = new Vehicle(2051, "Honda", "Odyssey");
        //execute
        ResponseEntity<Vehicle> responseEntity = restTemplate.postForEntity(getUrl(), vehicle, Vehicle.class);
        assertEquals(HttpStatus.PRECONDITION_FAILED.value(), responseEntity.getStatusCodeValue());

        Vehicle vehicle1 = new Vehicle(1949, "Honda", "Odyssey");
        //execute
        ResponseEntity<Vehicle> responseEntity1 = restTemplate.postForEntity(getUrl(), vehicle1, Vehicle.class);
        assertEquals(HttpStatus.PRECONDITION_FAILED.value(), responseEntity1.getStatusCodeValue());
    }

    @Test
    public void testUpdateVehicleWithId()
    {
        //prepare
        Vehicle vehicle0 = new Vehicle(2020, "toyota", "honda");
        ResponseEntity<Vehicle> R = restTemplate.postForEntity(getUrl(), vehicle0, Vehicle.class);

//get vehicle0 from database
        ResponseEntity<Vehicle> responseEntity =
                restTemplate.getForEntity(getUrl()+R.getBody().getId(), Vehicle.class); //notice the url is just localhost:8080/vehicles/
        assertEquals(HttpStatus.OK.value(), responseEntity.getStatusCodeValue());
        Vehicle v = responseEntity.getBody();
        //test the vehicle's values BEFORE being updated
        assertEquals(v.getYear(), 2020);
        assertEquals(v.getMake(), "toyota");
        assertEquals(v.getModel(), "honda");

        Vehicle vehicle0Updated = new Vehicle(2010, "chevy", "bolt");
        vehicle0Updated.setId(R.getBody().getId());
        restTemplate.put(getUrl()+R.getBody().getId(), vehicle0Updated, Vehicle.class); //update all vehicles in the database

        ResponseEntity<Vehicle> r1 = restTemplate.getForEntity(getUrl()+R.getBody().getId(), Vehicle.class);
        Vehicle vUpdated = r1.getBody();
        //test the vehicle's values AFTER being updated
        assertEquals(vUpdated.getYear(), 2010);
        assertEquals(vUpdated.getMake(), "chevy");
        assertEquals(vUpdated.getModel(), "bolt");
    }
    @Test
    public void testUpdateVehicles()
    {
        //prepare
        Vehicle vehicle0 = new Vehicle(2020, "toyota", "honda");
        Vehicle vehicle1 = new Vehicle(2020, "lightning", "bolt");
        restTemplate.postForEntity(getUrl(), vehicle0, Vehicle.class);
        restTemplate.postForEntity(getUrl(), vehicle1, Vehicle.class);

        //get vehicle1 from database
        ResponseEntity<Vehicle[]> responseEntity =
                restTemplate.getForEntity(getUrl(), Vehicle[].class); //notice the url is just localhost:8080/vehicles/
        Vehicle[] array = responseEntity.getBody(); //get the array of vehicles from database
        int[] idArray = new int[2]; //array of the vehicles' IDs
        for (int i = 0; i < array.length; i++)
        {
            idArray[i] = array[i].getId();
        }

        //UPDATE aka PUT
        Vehicle vehicle0Updated = new Vehicle(2000, "toyota", "prius");
        vehicle0Updated.setId(idArray[0]);
        Vehicle vehicle1Updated = new Vehicle(2010, "USAIN", "bolt");
        vehicle1Updated.setId(idArray[1]);

        //make a list of all the vehicles
        List<Vehicle> list = new ArrayList<>();
        list.add(vehicle0Updated);
        list.add(vehicle1Updated);

        restTemplate.put(getUrl(), list, ResponseEntity.class); //update all vehicles in the database

        //use GET to check that the vehicles in the database were indeed updated
        ResponseEntity<Vehicle[]> r1 =
                restTemplate.getForEntity(getUrl(), Vehicle[].class); //notice the url is just localhost:8080/vehicles/
        Vehicle[] a1 = r1.getBody(); //get the array of vehicles from database
        assertEquals(HttpStatus.OK.value(), r1.getStatusCodeValue());
        assertEquals(a1.length, 2);
        assertEquals(a1[0].getYear(), 2000);
        assertEquals(a1[1].getMake(), "USAIN");
        assertEquals(a1[0].getModel(), "prius");
    }

    @Test
    public void testDeleteVehicleWithIdFound()
    {
        //prepare
        Vehicle vehicle = new Vehicle(2050, "Honda", "Odyssey");
        ResponseEntity<Vehicle> responseEntity = restTemplate.postForEntity(getUrl(), vehicle, Vehicle.class);
        //execute
        restTemplate.delete(getUrl() + responseEntity.getBody().getId());
        //do a get to try to find the vehicle in the database
        ResponseEntity<Vehicle> r1 = restTemplate.getForEntity(getUrl() + responseEntity.getBody().getId(), Vehicle.class);
        int status = r1.getStatusCodeValue();
        assertEquals(HttpStatus.NOT_FOUND.value(), status);
    }

    @Test
    public void testDeleteVehicleWithIdNotFound()
    {
        //prepare
        Vehicle vehicle = new Vehicle(2050, "Honda", "Odyssey");
        ResponseEntity<Vehicle> responseEntity = restTemplate.postForEntity(getUrl(), vehicle, Vehicle.class);
        //execute
        restTemplate.delete(getUrl()+"-1");
        ResponseEntity<Vehicle> r1 = restTemplate.getForEntity(getUrl() + responseEntity.getBody().getId(), Vehicle.class);

        assertEquals(HttpStatus.OK.value(), r1.getStatusCodeValue());
    }
    @Test
    public void testGetVehicleWithId()
    {
        //prepare
        Vehicle vehicle = new Vehicle(2000, "Honda", "Odyssey");
        //execute
        ResponseEntity<Vehicle> responseEntity = restTemplate.postForEntity(getUrl(), vehicle, Vehicle.class);
        assertNotNull(responseEntity);

        Vehicle vehicleFromDatabase = restTemplate.getForObject(getUrl() + responseEntity.getBody().getId(), Vehicle.class);
        assertNotNull(vehicleFromDatabase);
        assertEquals(vehicle.getYear(), vehicleFromDatabase.getYear());
        assertEquals(vehicle.getMake(), vehicleFromDatabase.getMake());
        assertEquals(vehicle.getModel(), vehicleFromDatabase.getModel());
    }

    @Test
    public void testGetAllVehicles()
    {
        //prepare
        Vehicle vehicle0 = new Vehicle(2020, "toyota", "honda");
        Vehicle vehicle1 = new Vehicle(2020, "toyota", "honda");
        Vehicle vehicle2 = new Vehicle(2020, "chevy", "honda");
        Vehicle vehicle3 = new Vehicle(2020, "chevy", "honda");
        Vehicle vehicle4 = new Vehicle(2020, "chevy", "bolt");
        Vehicle vehicle5 = new Vehicle(2020, "lightning", "bolt");
        Vehicle []origArray = {vehicle0, vehicle1, vehicle2, vehicle3, vehicle4, vehicle5};
        restTemplate.postForEntity(getUrl(), vehicle0, Vehicle.class);
        restTemplate.postForEntity(getUrl(), vehicle1, Vehicle.class);
        restTemplate.postForEntity(getUrl(), vehicle2, Vehicle.class);
        restTemplate.postForEntity(getUrl(), vehicle3, Vehicle.class);
        restTemplate.postForEntity(getUrl(), vehicle4, Vehicle.class);
        restTemplate.postForEntity(getUrl(), vehicle5, Vehicle.class);

        ResponseEntity<Vehicle[]> responseEntity =
                restTemplate.getForEntity(getUrl(), Vehicle[].class); //notice the url is just localhost:8080/vehicles/
        Vehicle[] array = responseEntity.getBody(); //get the array of vehicles from database
        assertEquals(HttpStatus.OK.value(), responseEntity.getStatusCodeValue());
        assertEquals(array.length, 6);
        //test each vehicle's year make and model  from GET matches the respective year make and model of initial
        for (int i = 0; i < array.length; i++)
        {
            assertEquals(array[i].getYear(), origArray[i].getYear());
            assertEquals(array[i].getMake(), origArray[i].getMake());
            assertEquals(array[i].getModel(), origArray[i].getModel());
        }
    }
    @Test
    public void testGetFilterForYearThatDoesNotExist()
    {
        //prepare
        Vehicle vehicle = new Vehicle(2020, "toyota", "honda");
        Vehicle vehicle1 = new Vehicle(2020, "toyota", "honda");
        Vehicle vehicle2 = new Vehicle(2020, "chevy", "honda");
        Vehicle vehicle3 = new Vehicle(2020, "chevy", "honda");
        Vehicle vehicle4 = new Vehicle(2020, "chevy", "bolt");
        Vehicle vehicle5 = new Vehicle(2020, "lightning", "bolt");
        restTemplate.postForEntity(getUrl(), vehicle, Vehicle.class);
        restTemplate.postForEntity(getUrl(), vehicle1, Vehicle.class);
        restTemplate.postForEntity(getUrl(), vehicle2, Vehicle.class);
        restTemplate.postForEntity(getUrl(), vehicle3, Vehicle.class);
        restTemplate.postForEntity(getUrl(), vehicle4, Vehicle.class);
        restTemplate.postForEntity(getUrl(), vehicle5, Vehicle.class);

    //https://www.baeldung.com/spring-rest-template-list
        ResponseEntity<Vehicle[]> responseEntity =
                restTemplate.getForEntity(getUrl() + "?year=2000", Vehicle[].class);
        assertNotNull(responseEntity);
        Vehicle[] array = responseEntity.getBody(); //get the array of vehicles from database
        int status = responseEntity.getStatusCodeValue();
        assertEquals(HttpStatus.OK.value(), status);
        assertEquals(array.length, 0);
    }

    @Test
    public void testGetFilterByYearAllSameYear()
    {
        //prepare
        Vehicle vehicle0 = new Vehicle(2020, "toyota", "honda");
        Vehicle vehicle1 = new Vehicle(2020, "toyota", "honda");
        Vehicle vehicle2 = new Vehicle(2020, "chevy", "honda");
        Vehicle vehicle3 = new Vehicle(2020, "chevy", "honda");
        Vehicle vehicle4 = new Vehicle(2020, "chevy", "bolt");
        Vehicle vehicle5 = new Vehicle(2020, "lightning", "bolt");
        Vehicle []origArray = {vehicle0, vehicle1, vehicle2, vehicle3, vehicle4, vehicle5};


        restTemplate.postForEntity(getUrl(), vehicle0, Vehicle.class);
        restTemplate.postForEntity(getUrl(), vehicle1, Vehicle.class);
        restTemplate.postForEntity(getUrl(), vehicle2, Vehicle.class);
        restTemplate.postForEntity(getUrl(), vehicle3, Vehicle.class);
        restTemplate.postForEntity(getUrl(), vehicle4, Vehicle.class);
        restTemplate.postForEntity(getUrl(), vehicle5, Vehicle.class);

        //https://www.baeldung.com/spring-rest-template-list
        ResponseEntity<Vehicle[]> responseEntity =
                restTemplate.getForEntity(getUrl() + "?year=2020", Vehicle[].class);
        assertNotNull(responseEntity);
        Vehicle[] array = responseEntity.getBody(); //get the array of vehicles from database
        int status = responseEntity.getStatusCodeValue();
        //test that GET worked and that it returned an array of all the vehicles with year 2020
        assertEquals(HttpStatus.OK.value(), status);
        assertEquals(array.length, 6);
        //test each vehicle's year make and model  from GET matches the respective year make and model of initial
        for (int i = 0; i < array.length; i++)
        {
            assertEquals(array[i].getYear(), origArray[i].getYear());
            assertEquals(array[i].getMake(), origArray[i].getMake());
            assertEquals(array[i].getModel(), origArray[i].getModel());
        }
    }

    //Post a list of object with resttemplate https://www.baeldung.com/spring-rest-template-list
    @Test
    public void testGetFilterByYearNotAllSameYear()
    {
        //prepare
        Vehicle vehicle0 = new Vehicle(2020, "toyota", "honda");
        Vehicle vehicle1 = new Vehicle(2020, "toyota", "honda");
        Vehicle vehicle2 = new Vehicle(2020, "chevy", "honda");
        Vehicle vehicle3 = new Vehicle(2020, "chevy", "honda");
        Vehicle vehicle4 = new Vehicle(2010, "chevy", "bolt");
        Vehicle vehicle5 = new Vehicle(2010, "lightning", "bolt");
        Vehicle []origArray = {vehicle0, vehicle1, vehicle2, vehicle3, vehicle4, vehicle5};

        restTemplate.postForEntity(getUrl(), vehicle0, Vehicle.class);
        restTemplate.postForEntity(getUrl(), vehicle1, Vehicle.class);
        restTemplate.postForEntity(getUrl(), vehicle2, Vehicle.class);
        restTemplate.postForEntity(getUrl(), vehicle3, Vehicle.class);
        restTemplate.postForEntity(getUrl(), vehicle4, Vehicle.class);
        restTemplate.postForEntity(getUrl(), vehicle5, Vehicle.class);

        ResponseEntity<Vehicle[]> all =
                restTemplate.getForEntity(getUrl(), Vehicle[].class);

        //GET for /vehicles/?year=2020
        ResponseEntity<Vehicle[]> responseEntity =
                restTemplate.getForEntity(getUrl() + "?year=2020", Vehicle[].class);
        assertNotNull(responseEntity);
        Vehicle[] array2020 = responseEntity.getBody(); //get the array of vehicles from database
        int status = responseEntity.getStatusCodeValue();
        assertEquals(HttpStatus.OK.value(), status);
        assertEquals(array2020.length, 4); //check get returns 4 vehicles (the vehicles with year 2020)
        assertEquals(array2020[0].containsSameContentsAs(vehicle0), 1);
        assertEquals(array2020[1].containsSameContentsAs(vehicle1), 1);
        assertEquals(array2020[2].containsSameContentsAs(vehicle2), 1);
        assertEquals(array2020[3].containsSameContentsAs(vehicle3), 1);

        //GET for /vehicles/?year=2010
        ResponseEntity<Vehicle[]> responseEntity1 = restTemplate.getForEntity(getUrl() + "?year=2010", Vehicle[].class);
        Vehicle[] array2010 = responseEntity1.getBody();
        status = responseEntity1.getStatusCodeValue();
        assertEquals(HttpStatus.OK.value(), status);
        assertEquals(array2010.length, 2); //check get returns 2 vehicles for 2010
        assertNotEquals(array2010[0].containsSameContentsAs(vehicle0), 1);
        assertNotEquals(array2010[1].containsSameContentsAs(vehicle1), 1);

        //array2010[0] = vehicle4 and array2010[1] = vehicle5 since they have year 2010
        assertEquals(array2010[0].containsSameContentsAs(vehicle4), 1);
        assertEquals(array2010[1].containsSameContentsAs(vehicle5), 1);
    }

    @Test
    public void testGetFilterByYearAndMake()
    {
        //prepare
        Vehicle vehicle0 = new Vehicle(2020, "toyota", "honda");
        Vehicle vehicle1 = new Vehicle(2020, "toyota", "honda");
        Vehicle vehicle2 = new Vehicle(2020, "chevy", "honda");
        Vehicle vehicle3 = new Vehicle(2010, "chevy", "honda");
        Vehicle vehicle4 = new Vehicle(2010, "chevy", "bolt");
        Vehicle vehicle5 = new Vehicle(2010, "lightning", "bolt");
        Vehicle []origArray = {vehicle0, vehicle1, vehicle2, vehicle3, vehicle4, vehicle5};

        restTemplate.postForEntity(getUrl(), vehicle0, Vehicle.class);
        restTemplate.postForEntity(getUrl(), vehicle1, Vehicle.class);
        restTemplate.postForEntity(getUrl(), vehicle2, Vehicle.class);
        restTemplate.postForEntity(getUrl(), vehicle3, Vehicle.class);
        restTemplate.postForEntity(getUrl(), vehicle4, Vehicle.class);
        restTemplate.postForEntity(getUrl(), vehicle5, Vehicle.class);

        ResponseEntity<Vehicle[]> all =
                restTemplate.getForEntity(getUrl(), Vehicle[].class);
        //get
        ResponseEntity<Vehicle[]> responseEntity =
                restTemplate.getForEntity(getUrl() + "?year=2020&make=toyota", Vehicle[].class);
        Vehicle[] array = responseEntity.getBody(); //get the array of vehicles from database
        int status = responseEntity.getStatusCodeValue();
        //test that GET worked and that it returned an array of all the vehicles with year 2020
        assertEquals(HttpStatus.OK.value(), status);
        assertEquals(array.length, 2);
        assertEquals(array[0].containsSameContentsAs(vehicle0), 1);
        assertEquals(array[1].containsSameContentsAs(vehicle1), 1);


        ResponseEntity<Vehicle[]> r1 =
                restTemplate.getForEntity(getUrl() + "?year=2010&make=lightning", Vehicle[].class);
        Vehicle[] array2010Lightning = r1.getBody();
        assertEquals(HttpStatus.OK.value(), r1.getStatusCodeValue());
        assertEquals(array2010Lightning.length, 1);
        //check that the vehicle from GET equals vehicle5 since vehicle5 is the only one with year 2010, make lightning
        assertEquals(array2010Lightning[0].containsSameContentsAs(vehicle5), 1);
    }

    @Test
    public void testGetFilterByYearAndModel()
    {
        //prepare
        Vehicle vehicle0 = new Vehicle(2020, "toyota", "honda");
        Vehicle vehicle1 = new Vehicle(2020, "toyota", "honda");
        Vehicle vehicle2 = new Vehicle(2020, "chevy", "honda");
        Vehicle vehicle3 = new Vehicle(2010, "chevy", "honda");
        Vehicle vehicle4 = new Vehicle(2010, "chevy", "bolt");
        Vehicle vehicle5 = new Vehicle(2010, "lightning", "bolt");
        Vehicle []origArray = {vehicle0, vehicle1, vehicle2, vehicle3, vehicle4, vehicle5};


        restTemplate.postForEntity(getUrl(), vehicle0, Vehicle.class);
        restTemplate.postForEntity(getUrl(), vehicle1, Vehicle.class);
        restTemplate.postForEntity(getUrl(), vehicle2, Vehicle.class);
        restTemplate.postForEntity(getUrl(), vehicle3, Vehicle.class);
        restTemplate.postForEntity(getUrl(), vehicle4, Vehicle.class);
        restTemplate.postForEntity(getUrl(), vehicle5, Vehicle.class);

        ResponseEntity<Vehicle[]> all =
                restTemplate.getForEntity(getUrl(), Vehicle[].class);

        //get
        ResponseEntity<Vehicle[]> responseEntity =
                restTemplate.getForEntity(getUrl() + "?year=2020&model=honda", Vehicle[].class);
        Vehicle[] array = responseEntity.getBody(); //get the array of vehicles from database
        int status = responseEntity.getStatusCodeValue();
        //test that GET worked and that it returned an array of all the vehicles with year 2020
        assertEquals(HttpStatus.OK.value(), status);
        assertEquals(array.length, 3);
        assertEquals(array[0].containsSameContentsAs(vehicle0), 1);
        assertEquals(array[1].containsSameContentsAs(vehicle1), 1);
        assertEquals(array[2].containsSameContentsAs(vehicle2), 1);

        ResponseEntity<Vehicle[]> r1 =
                restTemplate.getForEntity(getUrl() + "?year=2010&model=honda", Vehicle[].class);
        Vehicle[] array2010Honda = r1.getBody();
        assertEquals(HttpStatus.OK.value(), r1.getStatusCodeValue());
        assertEquals(array2010Honda.length, 1);
        //check that the vehicle from GET equals vehicle5 since vehicle5 is the only one with year 2010, make lightning
        assertEquals(array2010Honda[0].containsSameContentsAs(vehicle3), 1);

        ResponseEntity<Vehicle[]> r2 =
                restTemplate.getForEntity(getUrl() + "?year=2010&model=bolt", Vehicle[].class);
        Vehicle[] array2010Bolt = r2.getBody();
        assertEquals(HttpStatus.OK.value(), r2.getStatusCodeValue());
        assertEquals(array2010Bolt.length, 2);
        //check that the vehicle from GET equals vehicle5 since vehicle5 is the only one with year 2010, make lightning
        assertEquals(array2010Bolt[0].containsSameContentsAs(vehicle4), 1);
        assertEquals(array2010Bolt[1].containsSameContentsAs(vehicle5), 1);
    }

    @Test
    public void testGetFilterByYearMakeModel()
    {
        //prepare
        Vehicle vehicle0 = new Vehicle(2020, "toyota", "honda");
        Vehicle vehicle1 = new Vehicle(2020, "toyota", "honda");
        Vehicle vehicle2 = new Vehicle(2020, "chevy", "honda");
        Vehicle vehicle3 = new Vehicle(2010, "chevy", "honda");
        Vehicle vehicle4 = new Vehicle(2010, "chevy", "bolt");
        Vehicle vehicle5 = new Vehicle(2010, "lightning", "bolt");
        Vehicle []origArray = {vehicle0, vehicle1, vehicle2, vehicle3, vehicle4, vehicle5};

        restTemplate.postForEntity(getUrl(), vehicle0, Vehicle.class);
        restTemplate.postForEntity(getUrl(), vehicle1, Vehicle.class);
        restTemplate.postForEntity(getUrl(), vehicle2, Vehicle.class);
        restTemplate.postForEntity(getUrl(), vehicle3, Vehicle.class);
        restTemplate.postForEntity(getUrl(), vehicle4, Vehicle.class);
        restTemplate.postForEntity(getUrl(), vehicle5, Vehicle.class);

        ResponseEntity<Vehicle[]> all = restTemplate.getForEntity(getUrl(), Vehicle[].class);
        //get
        ResponseEntity<Vehicle[]> responseEntity =
                restTemplate.getForEntity(getUrl() + "?year=2020&make=toyota&model=honda", Vehicle[].class);
        Vehicle[] array = responseEntity.getBody(); //get the array of vehicles from database
        int status = responseEntity.getStatusCodeValue();
        //test that GET worked and that it returned an array of all the vehicles with year 2020
        assertEquals(HttpStatus.OK.value(), status);
        assertEquals(array.length, 2);
        assertEquals(array[0].containsSameContentsAs(vehicle0), 1);
        assertEquals(array[1].containsSameContentsAs(vehicle1), 1);


        ResponseEntity<Vehicle[]> r1 =
                restTemplate.getForEntity(getUrl() + "?year=2010&make=chevy&model=honda", Vehicle[].class);
        Vehicle[] array2010Lightning = r1.getBody();
        assertEquals(HttpStatus.OK.value(), r1.getStatusCodeValue());
        assertEquals(array2010Lightning.length, 1);
        //check that the vehicle from GET equals vehicle5 since vehicle3 is the only year 2010, make lightning model honda
        assertEquals(array2010Lightning[0].containsSameContentsAs(vehicle3), 1);
    }

    @Test
    public void testGetFilterByMakeOnly()
    {
        //prepare
        Vehicle vehicle0 = new Vehicle(2020, "toyota", "honda");
        Vehicle vehicle1 = new Vehicle(2020, "toyota", "honda");
        Vehicle vehicle2 = new Vehicle(2010, "toyota", "prius");
        Vehicle vehicle3 = new Vehicle(2010, "chevy", "honda");
        Vehicle vehicle4 = new Vehicle(2010, "chevy", "bolt");
        Vehicle vehicle5 = new Vehicle(2010, "lightning", "bolt");
        Vehicle []origArray = {vehicle0, vehicle1, vehicle2, vehicle3, vehicle4, vehicle5};


        restTemplate.postForEntity(getUrl(), vehicle0, Vehicle.class);
        restTemplate.postForEntity(getUrl(), vehicle1, Vehicle.class);
        restTemplate.postForEntity(getUrl(), vehicle2, Vehicle.class);
        restTemplate.postForEntity(getUrl(), vehicle3, Vehicle.class);
        restTemplate.postForEntity(getUrl(), vehicle4, Vehicle.class);
        restTemplate.postForEntity(getUrl(), vehicle5, Vehicle.class);

        ResponseEntity<Vehicle[]> all = restTemplate.getForEntity(getUrl(), Vehicle[].class);

        //get
        ResponseEntity<Vehicle[]> responseEntity =
                restTemplate.getForEntity(getUrl() + "?make=toyota", Vehicle[].class);
        Vehicle[] array = responseEntity.getBody(); //get the array of vehicles from database
        int status = responseEntity.getStatusCodeValue();
        //test that GET worked and that it returned an array of all the vehicles with year 2020
        assertEquals(HttpStatus.OK.value(), status);
        assertEquals(3, array.length);
        assertEquals(array[0].containsSameContentsAs(vehicle0), 1);
        assertEquals(array[1].containsSameContentsAs(vehicle1), 1);
        assertEquals(array[2].containsSameContentsAs(vehicle2), 1);


        ResponseEntity<Vehicle[]> r1 =
                restTemplate.getForEntity(getUrl() + "?make=lightning", Vehicle[].class);
        Vehicle[] arrayLightning = r1.getBody();
        assertEquals(HttpStatus.OK.value(), r1.getStatusCodeValue());
        assertEquals(arrayLightning.length, 1);
        //check that the vehicle from GET equals vehicle5 since vehicle3 is the only year 2010, make lightning model honda
        assertEquals(arrayLightning[0].containsSameContentsAs(vehicle5), 1);

        ResponseEntity<Vehicle[]> r2 =
                restTemplate.getForEntity(getUrl() + "?make=chevy", Vehicle[].class);
        Vehicle[] arrayChevy = r2.getBody();
        assertEquals(HttpStatus.OK.value(), r2.getStatusCodeValue());
        assertEquals(arrayChevy.length, 2);
        //check that the vehicle from GET equals vehicle5 since vehicle3 is the only year 2010, make lightning model honda
        assertEquals(arrayChevy[0].containsSameContentsAs(vehicle3), 1);
        assertEquals(arrayChevy[1].containsSameContentsAs(vehicle4), 1);
    }

    @Test
    public void testGetFilterByMakeAndModel()
    {
        //prepare
        Vehicle vehicle0 = new Vehicle(2020, "toyota", "honda");
        Vehicle vehicle1 = new Vehicle(2020, "toyota", "honda");
        Vehicle vehicle2 = new Vehicle(2010, "toyota", "prius");
        Vehicle vehicle3 = new Vehicle(2010, "chevy", "honda");
        Vehicle vehicle4 = new Vehicle(2010, "chevy", "bolt");
        Vehicle vehicle5 = new Vehicle(2050, "chevy", "bolt");
        Vehicle []origArray = {vehicle0, vehicle1, vehicle2, vehicle3, vehicle4, vehicle5};


        restTemplate.postForEntity(getUrl(), vehicle0, Vehicle.class);
        restTemplate.postForEntity(getUrl(), vehicle1, Vehicle.class);
        restTemplate.postForEntity(getUrl(), vehicle2, Vehicle.class);
        restTemplate.postForEntity(getUrl(), vehicle3, Vehicle.class);
        restTemplate.postForEntity(getUrl(), vehicle4, Vehicle.class);
        restTemplate.postForEntity(getUrl(), vehicle5, Vehicle.class);

        ResponseEntity<Vehicle[]> all = restTemplate.getForEntity(getUrl(), Vehicle[].class);

        //get
        ResponseEntity<Vehicle[]> responseEntity =
                restTemplate.getForEntity(getUrl() + "?make=toyota&model=honda", Vehicle[].class);
        Vehicle[] array = responseEntity.getBody(); //get the array of vehicles from database
        int status = responseEntity.getStatusCodeValue();
        //test that GET worked and that it returned an array of all the vehicles with year 2020
        assertEquals(HttpStatus.OK.value(), status);
        assertEquals(array.length, 2);
        assertEquals(array[0].containsSameContentsAs(vehicle0), 1);
        assertEquals(array[1].containsSameContentsAs(vehicle1), 1);

        ResponseEntity<Vehicle[]> r1 =
                restTemplate.getForEntity(getUrl() + "?make=chevy&model=bolt", Vehicle[].class);
        Vehicle[] arrayLightning = r1.getBody(); //r1.getBody() should return an array containing vehicle4 and vehicle5
        assertEquals(HttpStatus.OK.value(), r1.getStatusCodeValue());
        assertEquals(arrayLightning.length, 2);
        //check that the vehicle from GET equals vehicle5 since vehicle3 is the only year 2010, make lightning model honda
        assertEquals(arrayLightning[0].containsSameContentsAs(vehicle4), 1);
        assertEquals(arrayLightning[1].containsSameContentsAs(vehicle5), 1);
    }

    @Test
    public void testGetFilterByModelOnly()
    {
        //prepare
        Vehicle vehicle0 = new Vehicle(2020, "toyota", "honda");
        Vehicle vehicle1 = new Vehicle(2020, "toyota", "honda");
        Vehicle vehicle2 = new Vehicle(2010, "chevy", "honda");
        Vehicle vehicle3 = new Vehicle(2010, "toyota", "prius");
        Vehicle vehicle4 = new Vehicle(2010, "chevy", "bolt");
        Vehicle vehicle5 = new Vehicle(2050, "chevy", "bolt");
        Vehicle []origArray = {vehicle0, vehicle1, vehicle2, vehicle3, vehicle4, vehicle5};

        restTemplate.postForEntity(getUrl(), vehicle0, Vehicle.class);
        restTemplate.postForEntity(getUrl(), vehicle1, Vehicle.class);
        restTemplate.postForEntity(getUrl(), vehicle2, Vehicle.class);
        restTemplate.postForEntity(getUrl(), vehicle3, Vehicle.class);
        restTemplate.postForEntity(getUrl(), vehicle4, Vehicle.class);
        restTemplate.postForEntity(getUrl(), vehicle5, Vehicle.class);

        ResponseEntity<Vehicle[]> all = restTemplate.getForEntity(getUrl(), Vehicle[].class);

        //get
        ResponseEntity<Vehicle[]> responseEntity =
                restTemplate.getForEntity(getUrl() + "?model=honda", Vehicle[].class);
        Vehicle[] array = responseEntity.getBody(); //get the array of vehicles with model honda from database
        int status = responseEntity.getStatusCodeValue();
        //test that GET worked and that it returned an array of all the vehicles with year 2020
        assertEquals(HttpStatus.OK.value(), status);
        assertEquals(array.length, 3);
        assertEquals(array[0].containsSameContentsAs(vehicle0), 1);
        assertEquals(array[1].containsSameContentsAs(vehicle1), 1);
        assertEquals(array[2].containsSameContentsAs(vehicle2), 1);

        ResponseEntity<Vehicle[]> r1 =
                restTemplate.getForEntity(getUrl() + "?model=bolt", Vehicle[].class);
        Vehicle[] arrayLightning = r1.getBody(); //r1.getBody() should return an array containing vehicle4 and vehicle5
        assertEquals(HttpStatus.OK.value(), r1.getStatusCodeValue());
        assertEquals(arrayLightning.length, 2);
        //check that the vehicle from GET equals vehicle5 since vehicle3 is the only year 2010, make lightning model honda
        assertEquals(arrayLightning[0].containsSameContentsAs(vehicle4), 1);
        assertEquals(arrayLightning[1].containsSameContentsAs(vehicle5), 1);
    }
}
