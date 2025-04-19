package com.devsuperior.dsmovie.services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.entities.MovieEntity;
import com.devsuperior.dsmovie.tests.MovieFactory;
import com.devsuperior.dsmovie.repositories.MovieRepository;
import com.devsuperior.dsmovie.services.exceptions.DatabaseException;
import com.devsuperior.dsmovie.services.exceptions.ResourceNotFoundException;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(SpringExtension.class)
public class MovieServiceTests {
	
	@InjectMocks
	private MovieService service;

	@Mock
	private MovieRepository movieRepository;

	private Long existingMovieId, nonExistindMovieId, dependentId;
	private MovieEntity movie;
	private MovieDTO movieDTO;

	private PageImpl<MovieEntity> page;

	@BeforeEach
	void setUp() throws Exception {

		existingMovieId = 1L;
		nonExistindMovieId = 2L;
		dependentId = 3L;

		movie = MovieFactory.createMovieEntity();
		movieDTO = new MovieDTO(movie);

		page = new PageImpl<>(List.of(movie));

		Mockito.when(movieRepository.searchByTitle((String) any(), (Pageable) any())).thenReturn(page);
		Mockito.when(movieRepository.findById(existingMovieId)).thenReturn(Optional.of(movie));
		Mockito.when(movieRepository.findById(nonExistindMovieId)).thenReturn(Optional.empty());
		Mockito.when(movieRepository.save(any())).thenReturn(movie);
		Mockito.when(movieRepository.getReferenceById(existingMovieId)).thenReturn(movie);
		Mockito.when(movieRepository.getReferenceById(nonExistindMovieId)).thenThrow(EntityNotFoundException.class);
		Mockito.when(movieRepository.existsById(existingMovieId)).thenReturn(true);
		Mockito.when(movieRepository.existsById(nonExistindMovieId)).thenReturn(false);
		Mockito.when(movieRepository.existsById(dependentId)).thenReturn(true);	
		Mockito.doNothing().when(movieRepository).deleteById(existingMovieId);
		Mockito.doThrow(DataIntegrityViolationException.class).when(movieRepository).deleteById(dependentId);

	}
	
	@Test
	public void findAllShouldReturnPagedMovieDTO() {
		Pageable pageable = PageRequest.of(0, 12);

		Page<MovieDTO> result = service.findAll("Test Movie", pageable);

		assertNotNull(result);
		//Mockito.verify(movieRepository, Mockito.times(1).findAll(pageable));

	}

	
	@Test
	public void findByIdShouldReturnMovieDTOWhenIdExists() {
		MovieDTO result = service.findById(existingMovieId);

		assertNotNull(result);
		assertEquals(existingMovieId, result.getId());
	}
	
	@Test
	public void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
		assertThrows(ResourceNotFoundException.class, () -> service.findById(nonExistindMovieId));
	}

	
	@Test
	public void insertShouldReturnMovieDTO() {
		MovieDTO result = service.insert(movieDTO);
		
		assertNotNull(result);
		assertEquals(movieDTO.getTitle(), result.getTitle());

	}

	
	@Test
	public void updateShouldReturnMovieDTOWhenIdExists() {
		MovieDTO result = service.update(existingMovieId, movieDTO);

		assertNotNull(result);
	}
	
	@Test
	public void updateShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
		assertThrows(ResourceNotFoundException.class, () -> service.update(nonExistindMovieId, movieDTO));
	}

	
	@Test
	public void deleteShouldDoNothingWhenIdExists() {
		assertDoesNotThrow(() -> service.delete(existingMovieId));
	}
	
	@Test
	public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
		assertThrows(ResourceNotFoundException.class, () -> service.delete(nonExistindMovieId));
	}
	
	@Test
	public void deleteShouldThrowDatabaseExceptionWhenDependentId() {
		assertThrows(DatabaseException.class, () -> service.delete(dependentId));
	}
}
