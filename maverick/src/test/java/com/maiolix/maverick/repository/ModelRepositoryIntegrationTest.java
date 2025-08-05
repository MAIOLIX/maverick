package com.maiolix.maverick.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.maiolix.maverick.entity.ModelEntity;
import com.maiolix.maverick.service.ModelDatabaseService;

/**
 * Test di integrazione per il ModelRepository e ModelDatabaseService con PostgreSQL
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ModelRepositoryIntegrationTest {

    @Autowired
    private ModelRepository modelRepository;

    @Autowired
    private ModelDatabaseService modelDatabaseService;

    private ModelEntity testModel;

    @BeforeEach
    void setUp() {
        // Pulisci il database
        modelRepository.deleteAll();

        // Crea un modello di test
        testModel = modelDatabaseService.createSampleModel(
                "iris-classifier", 
                "1.0.0", 
                ModelEntity.ModelType.ONNX
        );
    }

    @Test
    void testSaveAndRetrieveModel() {
        // Given
        ModelEntity saved = modelDatabaseService.saveModel(testModel);

        // When
        Optional<ModelEntity> retrieved = modelDatabaseService.findById(saved.getId());

        // Then
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getModelName()).isEqualTo("iris-classifier");
        assertThat(retrieved.get().getVersion()).isEqualTo("1.0.0");
        assertThat(retrieved.get().getType()).isEqualTo(ModelEntity.ModelType.ONNX);
        assertThat(retrieved.get().getStorageType()).isEqualTo(ModelEntity.StorageType.LOCAL);
        assertThat(retrieved.get().getIsActive()).isTrue();
        assertThat(retrieved.get().getModelUuid()).isNotNull();
    }

    @Test
    void testFindByNameAndVersion() {
        // Given
        modelDatabaseService.saveModel(testModel);

        // When
        Optional<ModelEntity> found = modelDatabaseService.findByNameAndVersion("iris-classifier", "1.0.0");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getModelName()).isEqualTo("iris-classifier");
        assertThat(found.get().getVersion()).isEqualTo("1.0.0");
    }

    @Test
    void testCloudModelStorage() {
        // Given
        ModelEntity cloudModel = modelDatabaseService.createSampleCloudModel(
                "fraud-detector", 
                "2.1.0",
                ModelEntity.StorageType.S3,
                "ml-models-bucket",
                "eu-west-1"
        );

        // When
        ModelEntity saved = modelDatabaseService.saveModel(cloudModel);

        // Then
        assertThat(saved.getStorageType()).isEqualTo(ModelEntity.StorageType.S3);
        assertThat(saved.getBucketName()).isEqualTo("ml-models-bucket");
        assertThat(saved.getBucketRegion()).isEqualTo("eu-west-1");
        assertThat(saved.getStorageClass()).isEqualTo("STANDARD");
        assertThat(saved.isCloudStorage()).isTrue();
        assertThat(saved.getFullUrl()).startsWith("s3://ml-models-bucket/");
    }

    @Test
    void testSearchByModelName() {
        // Given
        modelDatabaseService.saveModel(testModel);
        ModelEntity anotherModel = modelDatabaseService.createSampleModel(
                "sentiment-analyzer", 
                "1.0.0", 
                ModelEntity.ModelType.PMML
        );
        modelDatabaseService.saveModel(anotherModel);

        // When
        Page<ModelEntity> results = modelDatabaseService.searchByName("iris", 0, 10);

        // Then
        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getModelName()).isEqualTo("iris-classifier");
    }

    @Test
    void testFullTextSearch() {
        // Given
        modelDatabaseService.saveModel(testModel);

        // When
        Page<ModelEntity> results = modelDatabaseService.fullTextSearch("test", 0, 10);

        // Then
        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getDescription()).contains("test");
    }

    @Test
    void testIncrementPredictionCount() {
        // Given
        ModelEntity saved = modelDatabaseService.saveModel(testModel);
        Long initialCount = saved.getPredictionCount();

        // When
        modelDatabaseService.recordPrediction(saved.getId());

        // Then
        Optional<ModelEntity> updated = modelDatabaseService.findById(saved.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getPredictionCount()).isEqualTo(initialCount + 1);
        assertThat(updated.get().getLastUsedAt()).isNotNull();
    }

    @Test
    void testUpdateModelStatus() {
        // Given
        ModelEntity saved = modelDatabaseService.saveModel(testModel);

        // When
        boolean updated = modelDatabaseService.updateModelStatus(saved.getId(), ModelEntity.ModelStatus.MAINTENANCE);

        // Then
        assertThat(updated).isTrue();
        Optional<ModelEntity> retrieved = modelDatabaseService.findById(saved.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getStatus()).isEqualTo(ModelEntity.ModelStatus.MAINTENANCE);
    }

    @Test
    void testDeployNewVersion() {
        // Given
        ModelEntity v1 = testModel;
        modelDatabaseService.saveModel(v1);

        ModelEntity v2 = modelDatabaseService.createSampleModel(
                "iris-classifier", 
                "2.0.0", 
                ModelEntity.ModelType.ONNX
        );

        // When
        ModelEntity deployed = modelDatabaseService.deployNewVersion(v2);

        // Then
        assertThat(deployed.getVersion()).isEqualTo("2.0.0");
        assertThat(deployed.getIsActive()).isTrue();

        // Verifica che la v1 sia stata disattivata
        Optional<ModelEntity> oldVersion = modelDatabaseService.findByNameAndVersion("iris-classifier", "1.0.0");
        assertThat(oldVersion).isPresent();
        assertThat(oldVersion.get().getIsActive()).isFalse();
    }

    @Test
    void testFindByType() {
        // Given
        modelDatabaseService.saveModel(testModel);
        ModelEntity pmmlModel = modelDatabaseService.createSampleModel(
                "decision-tree", 
                "1.0.0", 
                ModelEntity.ModelType.PMML
        );
        modelDatabaseService.saveModel(pmmlModel);

        // When
        List<ModelEntity> onnxModels = modelDatabaseService.findByType(ModelEntity.ModelType.ONNX);
        List<ModelEntity> pmmlModels = modelDatabaseService.findByType(ModelEntity.ModelType.PMML);

        // Then
        assertThat(onnxModels).hasSize(1);
        assertThat(onnxModels.get(0).getModelName()).isEqualTo("iris-classifier");
        assertThat(pmmlModels).hasSize(1);
        assertThat(pmmlModels.get(0).getModelName()).isEqualTo("decision-tree");
    }

    @Test
    void testStatisticsQueries() {
        // Given
        modelDatabaseService.saveModel(testModel);
        ModelEntity cloudModel = modelDatabaseService.createSampleCloudModel(
                "fraud-detector", 
                "1.0.0",
                ModelEntity.StorageType.S3,
                "ml-bucket",
                "us-east-1"
        );
        modelDatabaseService.saveModel(cloudModel);

        // When
        List<Object[]> countByType = modelDatabaseService.getModelCountByType();
        List<Object[]> countByStorage = modelDatabaseService.getModelCountByStorageType();
        Long totalStorage = modelDatabaseService.getTotalStorageSize();

        // Then
        assertThat(countByType).hasSize(1); // Solo ONNX
        assertThat(countByStorage).hasSize(2); // LOCAL e S3
        assertThat(totalStorage).isGreaterThan(0);
    }

    @Test
    void testBucketOperations() {
        // Given
        ModelEntity s3Model = modelDatabaseService.createSampleCloudModel(
                "model-s3", 
                "1.0.0",
                ModelEntity.StorageType.S3,
                "s3-bucket",
                "us-east-1"
        );
        modelDatabaseService.saveModel(s3Model);

        ModelEntity azureModel = modelDatabaseService.createSampleCloudModel(
                "model-azure", 
                "1.0.0",
                ModelEntity.StorageType.AZURE_BLOB,
                "azure-container",
                "westeurope"
        );
        modelDatabaseService.saveModel(azureModel);

        // When
        List<String> s3Buckets = modelDatabaseService.getBucketsByStorageType(ModelEntity.StorageType.S3);
        List<String> azureBuckets = modelDatabaseService.getBucketsByStorageType(ModelEntity.StorageType.AZURE_BLOB);
        List<ModelEntity> cloudModels = modelDatabaseService.findCloudModels();

        // Then
        assertThat(s3Buckets).containsExactly("s3-bucket");
        assertThat(azureBuckets).containsExactly("azure-container");
        assertThat(cloudModels).hasSize(2);
    }

    @Test
    void testMaintenanceOperations() {
        // Given
        ModelEntity unusedModel = modelDatabaseService.createSampleModel(
                "unused-model", 
                "1.0.0", 
                ModelEntity.ModelType.ONNX
        );
        modelDatabaseService.saveModel(unusedModel);

        ModelEntity usedModel = testModel;
        ModelEntity saved = modelDatabaseService.saveModel(usedModel);
        modelDatabaseService.recordPrediction(saved.getId()); // Simula utilizzo

        // When
        List<ModelEntity> unused = modelDatabaseService.findUnusedModels();
        List<ModelEntity> stale = modelDatabaseService.findStaleModels(30);

        // Then
        assertThat(unused).hasSize(1);
        assertThat(unused.get(0).getModelName()).isEqualTo("unused-model");
        // I modelli appena creati non dovrebbero essere "stale"
        assertThat(stale).isEmpty();
    }
}
