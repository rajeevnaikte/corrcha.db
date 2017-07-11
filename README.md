# corrcha.db
Java DB adapter to connect and perform CRUD on Cassandra DB with feature to add audit entries into separate audit table simply with annotation @Audited on your entity class

# Sample
DBFacade dbFacade = DBFactory.getDataStaxAdapter();
dbFacade.connect("127.0.0.1", null, "audit", null, null);
dbFacade.parseEntity(EntityPojo.class);
EntityPojo entity = new EntityPojo();
dbFacade.save(entity);
dbFacade.disconnect();
