package com.devsuperior.dsmovie.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.dto.ScoreDTO;
import com.devsuperior.dsmovie.entities.MovieEntity;
import com.devsuperior.dsmovie.entities.ScoreEntity;
import com.devsuperior.dsmovie.entities.UserEntity;
import com.devsuperior.dsmovie.repositories.MovieRepository;
import com.devsuperior.dsmovie.repositories.ScoreRepository;
import com.devsuperior.dsmovie.tests.MovieFactory;
import com.devsuperior.dsmovie.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dsmovie.tests.ScoreFactory;
import com.devsuperior.dsmovie.tests.UserFactory;

@ExtendWith(SpringExtension.class)
public class ScoreServiceTests {
	
	@InjectMocks
	private ScoreService service;
	
	@Mock	
	private ScoreRepository scoreRepository;

	@Mock
	private UserService userService;
	
	@Mock
	private MovieRepository movieRepository;

	private Long existingMovieId, nonExistindMovieId;
	private MovieEntity movie;
	private MovieDTO movieDTO;
	private UserEntity user;
	private ScoreEntity score;
	private ScoreDTO scoreDTO;

	
	@BeforeEach
	void setUp() throws Exception {
		existingMovieId = 1L;
		nonExistindMovieId = 2L;

		movie = MovieFactory.createMovieEntity();
		movieDTO = new MovieDTO(movie);

		user = UserFactory.createUserEntity();

		score = ScoreFactory.createScoreEntity();
		scoreDTO = new ScoreDTO(score);

		//mock authenticated user should be inside each method, since they could be in diferent scenarios for each test
		Mockito.when(userService.authenticated()).thenReturn(user); //putting here because, in this specific case, I'm using the same authentication for both tests

		Mockito.when(movieRepository.findById(existingMovieId)).thenReturn(Optional.of(movie));
		Mockito.when(movieRepository.findById(nonExistindMovieId)).thenReturn(Optional.empty());
		
		Mockito.when(scoreRepository.saveAndFlush((ScoreEntity) any())).thenReturn(score);
		
		Mockito.when(movieRepository.save(any())).thenReturn(movie);
	}

	
	@Test
	public void saveScoreShouldReturnMovieDTO() {

		MovieDTO result = service.saveScore(scoreDTO);

		assertNotNull(result);
		assertEquals(existingMovieId, result.getId());
		
	}
	
	@Test
	public void saveScoreShouldThrowResourceNotFoundExceptionWhenNonExistingMovieId() {

		score.getId().getMovie().setId(nonExistindMovieId);
		scoreDTO = new ScoreDTO (score);
		assertThrows(ResourceNotFoundException.class, () -> service.saveScore(scoreDTO));

	}
}
