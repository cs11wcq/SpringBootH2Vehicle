package mitchell.SpringBootH2Vehicle;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Since we're using Spring Data JPA for saving users to the in-memory H2 database, we also need to
 * define a simple repository interface for having basic CRUD functionality on Vehicle objects
 * https://www.baeldung.com/spring-data-derived-queries
 * https://www.baeldung.com/spring-data-jpa-query
 */
public interface VehicleRepository extends CrudRepository<Vehicle, Integer>
{
    List<Vehicle> findByYearIs(Integer year);

    List<Vehicle> findByYearAndMake(Integer year, String make);

    List<Vehicle> findByYearAndModel(Integer year, String model);

    List<Vehicle> findByYearAndMakeAndModel(Integer year, String make, String model);

    List<Vehicle> findByMakeIs(String make);

    List<Vehicle> findByMakeAndModel(String make, String model);

    List<Vehicle> findByModelIs(String model);



//    List<Vehicle> findByYearBetween(Integer startYear, Integer endYear);
}
