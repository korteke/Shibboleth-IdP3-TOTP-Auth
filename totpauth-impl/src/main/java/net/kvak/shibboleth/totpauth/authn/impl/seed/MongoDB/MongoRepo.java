package net.kvak.shibboleth.totpauth.authn.impl.seed.MongoDB;

import org.springframework.data.mongodb.repository.MongoRepository;


public interface MongoRepo extends MongoRepository<MongoUser, String> {
	
    public MongoUser findByUserName(String userName);
    
    public int countByUserName(String userName);
    
    //public boolean existsByUserName(String userName);

}
