package com.popularmovies.vpaliy.data.mapper;

import com.google.common.annotations.VisibleForTesting;
import com.popularmovies.vpaliy.data.configuration.ImageQualityConfiguration;
import com.popularmovies.vpaliy.data.entity.BackdropImage;
import com.popularmovies.vpaliy.data.entity.Genre;
import com.popularmovies.vpaliy.data.entity.Movie;
import com.popularmovies.vpaliy.domain.model.MediaCover;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;


@Singleton
public class MovieMapper implements Mapper<MediaCover,Movie> {

    private final ImageQualityConfiguration qualityConfiguration;

    @Inject
    public MovieMapper(ImageQualityConfiguration qualityConfiguration){
        this.qualityConfiguration=qualityConfiguration;
    }

    @Override
    public MediaCover map(Movie movieEntity) {
        MediaCover cover=new MediaCover();
        cover.setMediaId(movieEntity.getMovieId());
        cover.setPosterPath(qualityConfiguration.convertCover(movieEntity.getPosterPath()));
        cover.setGenres(Genre.convert(movieEntity.getGenres()));
        cover.setMovieTitle(movieEntity.getTitle());
        cover.setAverageRate(movieEntity.getVoteAverage());
        cover.setFavorite(movieEntity.isFavorite());
        cover.setBackdrops(BackdropImage.convert(movieEntity.getBackdropImages(),qualityConfiguration));
        cover.setReleaseDate(movieEntity.getReleaseDate());
        cover.setDuration(convertToDuration(movieEntity.getRuntime()));
        String releaseTime=movieEntity.getReleaseDate();
        if(releaseTime!=null){
            cover.setReleaseDate(releaseTime);
            cover.setReleaseYear(releaseTime.substring(0,4));
        }
        return cover;
    }

    @Override
    public List<MediaCover> map(List<Movie> from) {
        if(from!=null) {
            List<MediaCover> coverList = new ArrayList<>(from.size());
            from.forEach(movie->coverList.add(map(movie)));
            return coverList;
        }
        return null;
    }

    @Override
    public Movie reverseMap(MediaCover movieCover) {
        Movie result=new Movie();
        result.setMovieId(movieCover.getMediaId());
        result.setPosterPath(qualityConfiguration.extractPath(movieCover.getPosterPath()));
        result.setGenres(Genre.convertToGenres(movieCover.getGenres()));
        result.setTitle(movieCover.getMovieTitle());
        result.setVoteAverage(movieCover.getAverageVote());
        result.setFavorite(movieCover.isFavorite());
        result.setRuntime(convertToRuntime(movieCover.getDuration()));
        result.setBackdropImages(BackdropImage.convertToBackdrops(movieCover.getBackdrops(), qualityConfiguration));
        result.setReleaseDate(movieCover.getReleaseDate());
        return result;
    }


    @VisibleForTesting
    int convertToRuntime(String duration){
        if(duration==null) return 0;
        Pattern pattern=Pattern.compile("-?\\d+");
        Matcher matcher=pattern.matcher(duration);
        int runtime=0;
        int count=0;
        while(matcher.find()) count++;
        matcher=matcher.reset();
        if(count==2){
            matcher.find();
            runtime=Integer.parseInt(matcher.group())*60;
            if(matcher.find()) runtime+=Integer.parseInt(matcher.group());
        }else if(matcher.find()) {
            runtime = Integer.parseInt(matcher.group());
            duration=duration.trim();
            if(duration.contains("hr")||duration.contains("hrs")) runtime*=60;
        }

        return runtime;
    }

    @VisibleForTesting
    String convertToDuration(int runtime){
        if(runtime>0) {
            int hours=runtime/60;
            String duration="";
            if(hours>0) {
                duration = Integer.toString(hours)+" hr"+(hours>1?"s ":" ");
            }
            int minRemainder=runtime % 60;
            if(minRemainder!=0){
                duration+=Integer.toString(minRemainder)+" min";
            }
            return duration;
        }
        return null;
    }

}
