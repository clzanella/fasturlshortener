package com.seniortest.fasturlshortener.service;

import com.seniortest.fasturlshortener.model.ShortURL;
import com.seniortest.fasturlshortener.repository.ShortURLRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShortURLService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShortURLService.class);
    private static final String URL_REGEX = "^(http:\\/\\/www\\.|https:\\/\\/www\\.|http:\\/\\/|https:\\/\\/)?[a-z0-9]+([\\-\\.]{1}[a-z0-9]+)*\\.[a-z]{2,5}(:[0-9]{1,5})?(\\/.*)?$";
    private static final Pattern URL_PATTERN = Pattern.compile(URL_REGEX);
    private static final long START_ID = Long.parseLong("lkjia", Character.MAX_RADIX);

    private final ShortURLRepository urlRepository;

    public ShortURLService(ShortURLRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    public Optional<ShortURL> findByStringID(String stringId){

        long id;

        try {
            id = stringToId(stringId);
        } catch (NumberFormatException exc){
            LOGGER.warn(String.format("Error parsing %s to long", stringId), exc);
            return Optional.empty();
        }

        return urlRepository.findById(id);
    }

    public Long stringToId(String stringId){
        return Long.parseLong(stringId, Character.MAX_RADIX) - START_ID;
    }

    public String idToString(long id){
        return Long.toString(id + START_ID, Character.MAX_RADIX);
    }

    public void save(ShortURL shortURL){
        urlRepository.save(shortURL);
    }

    public boolean validateURL(String url) {
        Matcher m = URL_PATTERN.matcher(url);
        return m.matches();
    }

}
