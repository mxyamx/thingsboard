/**
 * Copyright © 2016-2026 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.server.common.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.thingsboard.server.common.data.device.data.DeviceData;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.id.DeviceProfileId;
import org.thingsboard.server.common.data.id.OtaPackageId;
import org.thingsboard.server.common.data.id.TenantId;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DeviceTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    // =====================================================================
    // 1. Complétude fonctionnelle : Création et Propriétés de base
    // =====================================================================

    @Test
    void createDevice_shouldRetainAllProperties() {
        Device device = new Device();
        device.setName("Temperature Sensor");
        device.setType("sensor");
        device.setLabel("Building A - Floor 1");

        assertThat(device.getName()).isEqualTo("Temperature Sensor");
        assertThat(device.getType()).isEqualTo("sensor");
        assertThat(device.getLabel()).isEqualTo("Building A - Floor 1");
    }

    @Test
    void createDevice_withTenantAndCustomer_shouldRetainIds() {
        Device device = new Device();
        TenantId tenantId = new TenantId(UUID.randomUUID());
        CustomerId customerId = new CustomerId(UUID.randomUUID());

        device.setTenantId(tenantId);
        device.setCustomerId(customerId);
        device.setName("Gateway Device");
        device.setType("gateway");

        assertThat(device.getTenantId()).isEqualTo(tenantId);
        assertThat(device.getCustomerId()).isEqualTo(customerId);
    }

    @Test
    void createDevice_withDeviceId_shouldRetainIdAndEntityType() {
        DeviceId deviceId = new DeviceId(UUID.randomUUID());
        Device device = new Device(deviceId);

        assertThat(device.getId()).isEqualTo(deviceId);
        assertThat(device.getUuidId()).isEqualTo(deviceId.getId());
        assertThat(device.getId().getEntityType()).isEqualTo(EntityType.DEVICE);
    }

    @Test
    void deviceIdFromString_shouldParseValidUUID() {
        UUID uuid = UUID.randomUUID();
        DeviceId deviceId = DeviceId.fromString(uuid.toString());

        assertThat(deviceId).isNotNull();
        assertThat(deviceId.getId()).isEqualTo(uuid);
        assertThat(deviceId.getEntityType()).isEqualTo(EntityType.DEVICE);
    }

    // =====================================================================
    // 2. Fiabilité - Récupérabilité (Null Checks & Validation)
    // =====================================================================

    @Test
    void createDevice_withNullName_shouldAcceptNull() {
        Device device = new Device();
        device.setName(null);
        assertThat(device.getName()).isNull();
    }

    @Test
    void createDevice_withEmptyName_shouldAcceptEmpty() {
        Device device = new Device();
        device.setName("");
        assertThat(device.getName()).isEmpty();
    }

    @Test
    void createDevice_defaultValues_shouldBeNull() {
        Device device = new Device();
        assertThat(device.getName()).isNull();
        assertThat(device.getType()).isNull();
        assertThat(device.getLabel()).isNull();
        assertThat(device.getTenantId()).isNull();
        assertThat(device.getCustomerId()).isNull();
        assertThat(device.getId()).isNull();
    }

    @Test
    void createDevice_withAdditionalInfo_shouldHandleJsonNode() {
        Device device = new Device();
        ObjectNode additionalInfo = mapper.createObjectNode();
        additionalInfo.put("description", "Capteur installé au 3e étage");
        additionalInfo.put("gateway", false);

        device.setAdditionalInfo(additionalInfo);

        assertThat(device.getAdditionalInfo()).isNotNull();
        assertThat(device.getAdditionalInfo().get("description").asText())
                .isEqualTo("Capteur installé au 3e étage");
        assertThat(device.getAdditionalInfo().get("gateway").asBoolean()).isFalse();
    }

    @Test
    void createDevice_withNullAdditionalInfo_shouldAcceptNull() {
        Device device = new Device();
        device.setAdditionalInfo(null);

        // Vérifie que c'est soit Java null, soit un noeud JSON NullNode
        assertThat(device.getAdditionalInfo() == null || device.getAdditionalInfo().isNull())
                .as("Additional info should be null or a NullNode")
                .isTrue();
    }

    // =====================================================================
    // 3. Maintenabilité - Modifiabilité (Copie, Update, Sérialisation)
    // =====================================================================

    @Test
    void updateDeviceName_shouldReflectChange() {
        Device device = new Device();
        device.setName("Old Name");
        assertThat(device.getName()).isEqualTo("Old Name");

        device.setName("New Name");
        assertThat(device.getName()).isEqualTo("New Name");
    }

    @Test
    void copyConstructor_shouldCreateIndependentCopy() {
        Device original = new Device();
        original.setName("Original Sensor");
        original.setType("temperature");
        original.setLabel("Lab Room");
        TenantId tenantId = new TenantId(UUID.randomUUID());
        CustomerId customerId = new CustomerId(UUID.randomUUID());
        DeviceProfileId profileId = new DeviceProfileId(UUID.randomUUID());

        original.setTenantId(tenantId);
        original.setCustomerId(customerId);
        original.setDeviceProfileId(profileId);
        original.setVersion(1L);

        Device copy = new Device(original);

        assertThat(copy.getName()).isEqualTo("Original Sensor");
        assertThat(copy.getType()).isEqualTo("temperature");
        assertThat(copy.getLabel()).isEqualTo("Lab Room");
        assertThat(copy.getTenantId()).isEqualTo(tenantId);
        assertThat(copy.getCustomerId()).isEqualTo(customerId);
        assertThat(copy.getDeviceProfileId()).isEqualTo(profileId);
        assertThat(copy.getVersion()).isEqualTo(1L);

        copy.setName("Modified Sensor");
        assertThat(original.getName()).isEqualTo("Original Sensor");
        assertThat(copy.getName()).isEqualTo("Modified Sensor");
    }

    @Test
    void testUpdateDevice_FullCopy() {
        // Test complet de la méthode updateDevice(Device device) pour Line Coverage
        Device original = new Device();
        original.setName("Old");

        Device update = new Device();
        update.setName("New");
        update.setType("NewType");
        update.setLabel("NewLabel");
        update.setTenantId(new TenantId(UUID.randomUUID()));
        update.setCustomerId(new CustomerId(UUID.randomUUID()));
        update.setDeviceProfileId(new DeviceProfileId(UUID.randomUUID()));
        update.setFirmwareId(new OtaPackageId(UUID.randomUUID()));
        update.setSoftwareId(new OtaPackageId(UUID.randomUUID()));
        update.setExternalId(new DeviceId(UUID.randomUUID()));
        update.setVersion(2L);

        ObjectNode info = mapper.createObjectNode();
        info.put("test", "value");
        update.setAdditionalInfo(info);

        original.updateDevice(update);

        assertThat(original.getName()).isEqualTo("New");
        assertThat(original.getType()).isEqualTo("NewType");
        assertThat(original.getTenantId()).isEqualTo(update.getTenantId());
        assertThat(original.getExternalId()).isEqualTo(update.getExternalId());
        assertThat(original.getFirmwareId()).isEqualTo(update.getFirmwareId());
        assertThat(original.getVersion()).isEqualTo(2L);
        assertThat(original.getAdditionalInfo().get("test").asText()).isEqualTo("value");
    }

    @Test
    void deviceSerialization_shouldProduceValidJson() throws Exception {
        Device device = new Device();
        device.setName("JSON Test Device");
        device.setType("default");
        device.setLabel("Test Label");

        String json = mapper.writeValueAsString(device);
        assertThat(json).contains("JSON Test Device");
        assertThat(json).contains("default");

        Device deserialized = mapper.readValue(json, Device.class);
        assertThat(deserialized.getName()).isEqualTo("JSON Test Device");
        assertThat(deserialized.getType()).isEqualTo("default");
    }

    // =====================================================================
    // 4. Branch Coverage & Méthodes Générées (Equals, HashCode, ToString)
    // =====================================================================

    @Test
    void testEqualsAndHashCode() {
        DeviceId id = new DeviceId(UUID.randomUUID());
        Device d1 = new Device();
        d1.setId(id);
        d1.setName("D1");
        d1.setType("Sensor");
        d1.setVersion(1L);

        Device d2 = new Device();
        d2.setId(id);
        d2.setName("D1");
        d2.setType("Sensor");
        d2.setVersion(1L);

        Device d3 = new Device();
        d3.setName("Diff");

        assertThat(d1).isEqualTo(d1); // Réflexivité
        assertThat(d1).isEqualTo(d2); // Symétrie
        assertThat(d2).isEqualTo(d1);
        assertThat(d1).isNotEqualTo(d3); // Différence
        assertThat(d1).isNotEqualTo(null);
        assertThat(d1).isNotEqualTo(new Object());

        assertThat(d1.hashCode()).isEqualTo(d2.hashCode());
        assertThat(d1.hashCode()).isNotEqualTo(d3.hashCode());
    }

    @Test
    void testToString() {
        Device d = new Device();
        d.setName("StringTestDevice");
        String str = d.toString();
        assertThat(str).isNotNull();
        assertThat(str).contains("Device");
        assertThat(str).contains("StringTestDevice");
    }

    // =====================================================================
    // 5. ZONE EXPERTE : Tests Logiciels Complexes (OwnerId, Lazy Loading)
    // =====================================================================

    @Test
    void testOwnerId_Branches() {
        Device device = new Device();
        TenantId tenantId = new TenantId(UUID.randomUUID());
        CustomerId validCustomerId = new CustomerId(UUID.randomUUID());

        // Cas 1: Customer ID est NULL -> Doit retourner TenantId
        device.setTenantId(tenantId);
        device.setCustomerId(null);
        assertThat(device.getOwnerId()).isEqualTo(tenantId);

        // Cas 2: Customer ID est VALIDE -> Doit retourner CustomerId
        device.setCustomerId(validCustomerId);
        assertThat(device.getOwnerId()).isEqualTo(validCustomerId);

        // Cas 3: Customer ID est "Null UUID" (ThingsBoard specific logic)
        // Ceci teste la condition !customerId.isNullUid()
        CustomerId nullUidCustomer = new CustomerId(UUID.fromString("13814000-1dd2-11b2-8080-808080808080"));
        device.setCustomerId(nullUidCustomer);
        assertThat(device.getOwnerId()).isEqualTo(tenantId);
    }

    @Test
    void testGetDeviceData_DeserializationAndExceptions() {
        Device device = new Device();

        // Cas 1: deviceData est null au départ
        assertThat(device.getDeviceData()).isNull();

        // Cas 2: setDeviceData avec un objet valide
        DeviceData data = new DeviceData();
        // Note: Assumant que DeviceData est un POJO accessible
        device.setDeviceData(data);
        assertThat(device.getDeviceData()).isNotNull();
        assertThat(device.getDeviceDataBytes()).isNotNull(); // Vérifie que la sérialisation a eu lieu

        // Cas 3: Reconstruction depuis les bytes (Lazy Loading)
        Device fromBytes = new Device();
        fromBytes.setDeviceDataBytes(device.getDeviceDataBytes());
        assertThat(fromBytes.getDeviceData()).isNotNull();

        // Cas 4: Bytes corrompus -> Doit déclencher le catch(IOException)
        Device invalidBytesDevice = new Device();
        invalidBytesDevice.setDeviceDataBytes("Ceci n'est pas du JSON valide".getBytes());
        // Doit retourner null car l'exception est catchée et loggée
        assertThat(invalidBytesDevice.getDeviceData()).isNull();
    }

    @Test
    void testOtaAndExternalIds() {
        // Couverture des getters/setters simples oubliés
        Device device = new Device();
        OtaPackageId fwId = new OtaPackageId(UUID.randomUUID());
        OtaPackageId swId = new OtaPackageId(UUID.randomUUID());
        DeviceId extId = new DeviceId(UUID.randomUUID());

        device.setFirmwareId(fwId);
        device.setSoftwareId(swId);
        device.setExternalId(extId);

        assertThat(device.getFirmwareId()).isEqualTo(fwId);
        assertThat(device.getSoftwareId()).isEqualTo(swId);
        assertThat(device.getExternalId()).isEqualTo(extId);
    }
}