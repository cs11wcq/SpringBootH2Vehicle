package mitchell.SpringBootH2Vehicle;

import mitchell.SpringBootH2Vehicle.exception.InvalidYearException;
import mitchell.SpringBootH2Vehicle.exception.LeaveOutIDException;
import mitchell.SpringBootH2Vehicle.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Seth Kim
 * 2/14/2020
 * Mitchell International
 * SpringBoot Coding Project
 */
@RestController //marks this class as a web controller
public class VehicleController {

    @Autowired
    VehicleRepository vehicleRepository;

//    //https://www.javatpoint.com/spring-boot-annotations
//    @GetMapping("/vehicles") //used instead of @RequestMapping(method = RequestMethod.GET)
//    public List<Vehicle> getAllVehicles()
//    {
//        List<Vehicle> vehicles = new ArrayList<Vehicle>();
//        vehicleRepository.findAll().forEach(vehicle -> vehicles.add(vehicle));
//        return vehicles;
//    }

    @GetMapping("/vehicles/{id}")
    public Vehicle getVehicleById(@PathVariable("id") int id) throws ResourceNotFoundException
    {
        //if h2 cannot find the vehicle with id "id" in database, then throw ResourceNotFoundException
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: "+id));
        //if vehicle was found, return that vehicle
        return vehicleRepository.findById(id).get();
    }

    /**
     * optional filtering. The getVehicles() should support filtering vehicles based on one or more
     * vehicle properties (Ex: retrieving all vehicles where 'make' is 'Toyota')
     * https://www.baeldung.com/spring-request-param   can also map a multi-val param. refer to section 7
     * /vehicles/?year={yearParam}&make={makeParam}&model={modelParam}
     * ex: /vehicles/?year=2010&make=toyota&model=prius
     * @return
     * @throws ResourceNotFoundException
     */
    @GetMapping("/vehicles/")
    public ResponseEntity<List<Vehicle>> getVehicleFilter(@RequestParam(name="year", required = false) String yearParam,
                                          @RequestParam(name="make", required = false) String makeParam,
                                          @RequestParam(name="model",required = false) String modelParam)
    {
        //note: if no year, make, model param specified, this method will simulate getAllVehicles()
        //if year does not show up in the url, then yearParam gets set to null (likewise with make and model)
        List<Vehicle> vehicles = new ArrayList<Vehicle>();
        //{year}
        //could have used findByYearBetween(Integer startYear, Integer endYear)
        if (yearParam != null && makeParam == null && modelParam == null)
        {
            //ex: /vehicles/?year=2010&make=toyota&model=prius   year=2010 so yearParam = "2010". Must convert to int
            //so database can find all vehicles with year 2010
            System.out.println("{year}");
            //note: must convert yearParam to an int so that vehicleRepository can find all vehicles with that year
            vehicleRepository.findByYearIs(Integer.parseInt(yearParam)).forEach(vehicle -> vehicles.add(vehicle));
        }
        //{year}/{make}
        else if (yearParam != null && makeParam != null && modelParam == null)
        {
            System.out.println("{year}/{make}");
            vehicleRepository.findByYearAndMake(Integer.parseInt(yearParam), makeParam).forEach(vehicle -> vehicles.add(vehicle));
        }
        //{year}/{model}
        else if (yearParam != null && makeParam == null && modelParam != null)
        {
            System.out.println("{year}/{model}");
            vehicleRepository.findByYearAndModel(Integer.parseInt(yearParam), modelParam).forEach(vehicle -> vehicles.add(vehicle));
        }
        //{year}/{make}/{model}
        else if (yearParam != null && makeParam != null && modelParam != null)
        {
            System.out.println("{year}/{make}/{model}");
            vehicleRepository.findByYearAndMakeAndModel(Integer.parseInt(yearParam), makeParam, modelParam).forEach(vehicle -> vehicles.add(vehicle));
        }
        //{make}
        else if (yearParam == null && makeParam != null && modelParam == null)
        {
            System.out.println("{make}");
            vehicleRepository.findByMakeIs(makeParam).forEach(vehicle -> vehicles.add(vehicle));
        }
        //{make}/{model}
        else if (yearParam == null && makeParam != null && modelParam != null)
        {
            System.out.println("{make}/{model}");
            vehicleRepository.findByMakeAndModel(makeParam, modelParam).forEach(vehicle -> vehicles.add(vehicle));
        }
        //{model}
        else if (yearParam == null && makeParam == null && modelParam != null)
        {
            System.out.println("{model}");
            vehicleRepository.findByModelIs(modelParam).forEach(vehicle -> vehicles.add(vehicle));
        }
        //get all vehicles
        else if (yearParam == null && makeParam == null && modelParam == null)
        {
            vehicleRepository.findAll().forEach(vehicle -> vehicles.add(vehicle));
        }
        return new ResponseEntity<List<Vehicle>>(vehicles, HttpStatus.OK);
    }


    @DeleteMapping("/vehicles/{id}")
    public Map<String, Boolean> deleteVehicle(@PathVariable("id") int id) throws ResourceNotFoundException
    {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));
        vehicleRepository.deleteById(id);

        //below is simply to return in the form {"deleted": true)
        Map< String, Boolean > response = new HashMap< >();
        response.put("deleted", Boolean.TRUE);
        return response;
    }

    /**
     * Note: the @Valid is used to make sure the Vehicle created avoids
     *       non-null/non-empty make and model
     * https://springframework.guru/using-resttemplate-in-spring/
     * @param vehicle
     * @return
     */
    @PostMapping("/vehicles")
    public Vehicle createVehicle(@Valid @RequestBody Vehicle vehicle) throws InvalidYearException, LeaveOutIDException
    {
        //if postman enters an ID in the response body that already exists in the database,
        //then don't follow through since this would cause a PUT to happen rather than a POST
        if (vehicleRepository.findById(vehicle.getId()).isPresent())
        {
            throw new LeaveOutIDException("Please leave out the vehicle ID when creating a vehicle." +
                    "If you want to update a vehicle, then call UPDATE");
        }

        //validate the year of vehicle is btwn 1950-2050
        if (vehicle.getYear() < 1950 || vehicle.getYear() > 2050)
        {
            throw new InvalidYearException("Vehicle's year must be between 1950 and 2050." +
                    "Year " + vehicle.getYear() + " is invalid!");
        }
        vehicleRepository.save(vehicle);
//        return new ResponseEntity<Vehicle>(vehicle, HttpStatus.CREATED);
        return vehicle;
    }

    //@Valid see below link
    // https://stackoverflow.com/questions/3595160/what-does-the-valid-annotation-indicate-in-spring
    //https://www.baeldung.com/spring-boot-bean-validation
    @PutMapping("/vehicles/{id}")
    public Vehicle updateVehicle(@PathVariable("id") int id,
                                 @Valid @RequestBody Vehicle vehicleDetails) throws ResourceNotFoundException, InvalidYearException
    {
//        System.out.println("VEHICLE DETAILS ID: " + vehicleDetails.getId()); //null or 0?
        //guarantee the vehicle is in the database. if it is not, thrown an exception
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found at id: " + id));
        //validate the year of vehicle is btwn 1950-2050
        if (vehicleDetails.getYear() < 1950 || vehicleDetails.getYear() > 2050)
        {
            throw new InvalidYearException("Vehicle's year must be between 1950 and 2050." +
                    "Year " + vehicle.getYear() + " is invalid!");
        }
        //update the vehicle (with ID id in the database) to contain the contents of vehicleDetails
        vehicle.setYear(vehicleDetails.getYear());
        vehicle.setMake(vehicleDetails.getMake());
        vehicle.setModel(vehicleDetails.getModel());
        //save the updated person in the database
        //also return the updated contents of that vehicle
        vehicleRepository.save(vehicle);
        return vehicle;
    }

    /**
     * UPDATE vehicles
     * @return a response entity containing the body (the list of vehicles) and the status code
     */
    @PutMapping("/vehicles")
    public ResponseEntity<List<Vehicle>> updateVehicles(@RequestBody List<Vehicle> vehicles)
            throws ResourceNotFoundException, InvalidYearException
    {
        List<Vehicle> list = new ArrayList<>();
        //update all of the vehicles and add each updated vehicle to list
        for (Vehicle vehicleDetails: vehicles)
        {
            int id = vehicleDetails.getId(); //get each vehicle's id so we know which vehicle to update
            list.add(updateVehicle(id, vehicleDetails));
        }
        return new ResponseEntity<List<Vehicle>>(list, HttpStatus.OK);
    }
}
