package com.forthestreets.venueservice.service;

import com.forthestreets.venueservice.domain.Venue;
import com.forthestreets.venueservice.dto.VenueRequest;
import com.forthestreets.venueservice.dto.VenueResponse;
import com.forthestreets.venueservice.exception.VenueNotFoundException;
import com.forthestreets.venueservice.repository.VenueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BarServiceTest {
    @Mock
    private VenueRepository venueRepository;

    @InjectMocks
    private BarService venueService;

    private GeometryFactory geometryFactory;

    @BeforeEach
    void setUp() {
        // SRID 4326 is standard GPS coordinates projection
        geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    }


    @Nested
    @DisplayName("Create Venue Tests")
    class CreateVenueTests {

        @Test
        @DisplayName("Success: Should save and map valid VenueRequest to a spatial Point structure")
        void shouldCreateVenueSuccessfully() {
            VenueRequest request = new VenueRequest("The Miracle Theater", "Inglewood, CA", 33.9617, -118.3533);
            Point locationPoint = geometryFactory.createPoint(new Coordinate(-118.3533, 33.9617));

            Venue savedVenue = new Venue();
            savedVenue.setId(10L);
            savedVenue.setName(request.name());
            savedVenue.setAddress(request.address());
            savedVenue.setLocation(locationPoint);

            when(venueRepository.save(any(Venue.class))).thenReturn(savedVenue);

            VenueResponse response = venueService.createVenue(request);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(10L);
            assertThat(response.name()).isEqualTo("The Miracle Theater");
            assertThat(response.latitude()).isEqualTo(33.9617);
            assertThat(response.longitude()).isEqualTo(-118.3533);
            verify(venueRepository, times(1)).save(any(Venue.class));
        }
    }

    @Nested
    @DisplayName("Get Venue By ID Tests")
    class GetVenueByIdTests {

        @Test
        @DisplayName("Success: Should return correct VenueResponse when matching database record exists")
        void shouldReturnVenueWhenExists() {
            Long venueId = 42L;
            Point locationPoint = geometryFactory.createPoint(new Coordinate(-118.3734, 33.9678));
            Venue venue = new Venue();
            venue.setId(venueId);
            venue.setName("Three Weavers Brewing");
            venue.setAddress("Inglewood");
            venue.setLocation(locationPoint);

            when(venueRepository.findById(venueId)).thenReturn(Optional.of(venue));

            VenueResponse response = venueService.getVenueById(venueId);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(venueId);
            assertThat(response.name()).isEqualTo("Three Weavers Brewing");
            assertThat(response.latitude()).isEqualTo(33.9678);
            assertThat(response.longitude()).isEqualTo(-118.3734);
        }

        @Test
        @DisplayName("Exception: Should throw custom VenueNotFoundException when database ID is invalid")
        void shouldThrowExceptionWhenVenueNotFound() {

            Long venueId = 999L;
            when(venueRepository.findById(venueId)).thenReturn(Optional.empty());


            assertThatThrownBy(() -> venueService.getVenueById(venueId))
                    .isInstanceOf(VenueNotFoundException.class)
                    .hasMessageContaining("Venue with ID " + venueId + " could not be found");

            verify(venueRepository, times(1)).findById(venueId);
        }
    }

    @Nested
    @DisplayName("Nearby Venues Spatial Query Tests")
    class NearbyVenuesTests {

        @Test
        @DisplayName("Success: Should return populated list of mapped nearby VenueResponses")
        void shouldReturnNearbyVenues() {
            double latitude = 33.9456;
            double longitude = -118.3418;
            double radiusInMeters = 5000.0;

            Point locationPoint = geometryFactory.createPoint(new Coordinate(-118.3533, 33.9617));
            Venue venue = new Venue();
            venue.setId(1L);
            venue.setName("Close Spot");
            venue.setLocation(locationPoint);

            when(venueRepository.findVenuesNearby(latitude, longitude, radiusInMeters))
                    .thenReturn(List.of(venue));

            List<VenueResponse> response = venueService.getVenuesNearby(latitude, longitude, radiusInMeters);

            assertThat(response).hasSize(1);
            assertThat(response.getFirst().name()).isEqualTo("Three Weavers");
            assertThat(response.getFirst().latitude()).isEqualTo(33.9678);
            assertThat(response.getFirst().longitude()).isEqualTo(-118.3734);

            verify(venueRepository, times(1)).findVenuesNearby(latitude, longitude, radiusInMeters);
        }

        @Test
        @DisplayName("Success: Should return empty list when no venues sit inside coordinates range boundary")
        void shouldReturnEmptyListWhenNoVenuesNearby() {
            double latitude = 33.9456;
            double longitude = -118.3418;
            double radiusInMeters = 5000.0;

            when(venueRepository.findVenuesNearby(anyDouble(), anyDouble(), anyDouble()))
                    .thenReturn(Collections.emptyList());


            List<VenueResponse> response = venueService.getVenuesNearby(latitude, longitude, radiusInMeters);

            assertThat(response).isEmpty();
            verify(venueRepository, times(1)).findVenuesNearby(latitude, longitude, radiusInMeters);
        }
    }

    @Nested
    @DisplayName("Update Venue Tests")
    class UpdateVenueTests {

        @Test
        @DisplayName("Success: Should update entity fields and recalculate spatial coordinates point successfully")
        void shouldUpdateVenueSuccessfully() {
            // Given
            Long venueId = 5L;
            VenueRequest updateRequest = new VenueRequest("Updated Name", "New Road, CA", 34.0, -118.0);

            Point oldLocation = geometryFactory.createPoint(new Coordinate(-118.5, 33.5));
            Venue existingVenue = new Venue();
            existingVenue.setId(venueId);
            existingVenue.setName("Old Name");
            existingVenue.setAddress("Old Road");
            existingVenue.setLocation(oldLocation);

            when(venueRepository.findById(venueId)).thenReturn(Optional.of(existingVenue));
            when(venueRepository.save(any(Venue.class))).thenAnswer(invocation -> invocation.getArgument(0));

            VenueResponse response = venueService.updateVenue(venueId, updateRequest);

            assertThat(response).isNotNull();
            assertThat(response.name()).isEqualTo("Updated Name");
            assertThat(response.latitude()).isEqualTo(34.0);
            assertThat(response.longitude()).isEqualTo(-118.0);
        }
    }

    @Nested
    @DisplayName("Delete Venue Tests")
    class DeleteVenueTests {

        @Test
        @DisplayName("Success: Should trigger repository deletion sequence when ID exists in DB")
        void shouldDeleteVenueSuccessfully() {
            // Given
            Long idToDelete = 15L;
            when(venueRepository.existsById(idToDelete)).thenReturn(true);
            doNothing().when(venueRepository).deleteById(idToDelete);

            // When
            venueService.deleteVenue(idToDelete);

            // Then
            verify(venueRepository, times(1)).deleteById(idToDelete);
        }

        @Test
        @DisplayName("Exception: Should raise VenueNotFoundException during deletion if ID does not exist")
        void shouldThrowExceptionWhenDeletingNonexistentVenue() {
            // Given
            Long badId = 999L;
            when(venueRepository.existsById(badId)).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> venueService.deleteVenue(badId))
                    .isInstanceOf(VenueNotFoundException.class)
                    .hasMessageContaining("Cannot delete: Venue not found with ID: " + badId);

            verify(venueRepository, never()).deleteById(anyLong());
        }
    }
}