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
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.TenantId;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    // =====================================================================
    // 1. Complétude fonctionnelle : Création et Champs de base
    // =====================================================================

    @Test
    void createCustomer_shouldRetainTitleAndTenant() {
        Customer customer = new Customer();
        customer.setTitle("Hydro-Québec");
        TenantId tenantId = new TenantId(UUID.randomUUID());
        customer.setTenantId(tenantId);

        assertThat(customer.getTitle()).isEqualTo("Hydro-Québec");
        // Test de l'alias getName() pour la rétrocompatibilité
        assertThat(customer.getName()).isEqualTo("Hydro-Québec");
        assertThat(customer.getTenantId()).isEqualTo(tenantId);
    }

    @Test
    void createCustomer_withId_shouldRetainIdAndEntityType() {
        CustomerId customerId = new CustomerId(UUID.randomUUID());
        Customer customer = new Customer(customerId);

        assertThat(customer.getId()).isEqualTo(customerId);
        assertThat(customer.getId().getEntityType()).isEqualTo(EntityType.CUSTOMER);
    }

    @Test
    void createCustomer_withContactInfo_shouldRetainAllFields() {
        Customer customer = new Customer();
        customer.setTitle("Desjardins");
        customer.setEmail("admin@desjardins.com");
        customer.setPhone("+1-514-555-0100");
        customer.setCountry("Canada");
        customer.setCity("Lévis");
        customer.setState("Québec");
        customer.setZip("G6V 6P9");
        customer.setAddress("100 rue des Commandeurs");
        customer.setAddress2("Suite 200");

        assertThat(customer.getEmail()).isEqualTo("admin@desjardins.com");
        assertThat(customer.getPhone()).isEqualTo("+1-514-555-0100");
        assertThat(customer.getCountry()).isEqualTo("Canada");
        assertThat(customer.getCity()).isEqualTo("Lévis");
        assertThat(customer.getState()).isEqualTo("Québec");
        assertThat(customer.getZip()).isEqualTo("G6V 6P9");
        assertThat(customer.getAddress()).isEqualTo("100 rue des Commandeurs");
        assertThat(customer.getAddress2()).isEqualTo("Suite 200");
    }

    // =====================================================================
    // 2. Logique Métier : isPublic() et ShortInfo
    // =====================================================================

    @Test
    void testIsPublic_LogicBranches() {
        Customer customer = new Customer();

        // Cas 1: additionalInfo est null -> false
        customer.setAdditionalInfo(null);
        assertThat(customer.isPublic()).isFalse();

        // Cas 2: additionalInfo existe mais vide -> false
        ObjectNode info = mapper.createObjectNode();
        customer.setAdditionalInfo(info);
        assertThat(customer.isPublic()).isFalse();

        // Cas 3: additionalInfo contient "isPublic": false -> false
        info.put("isPublic", false);
        assertThat(customer.isPublic()).isFalse();

        // Cas 4: additionalInfo contient "isPublic": true -> true
        info.put("isPublic", true);
        assertThat(customer.isPublic()).isTrue();
    }

    @Test
    void testToShortCustomerInfo() {
        // Test de la conversion vers l'objet léger (DTO)
        CustomerId id = new CustomerId(UUID.randomUUID());
        Customer customer = new Customer(id);
        customer.setTitle("Big Corp");

        // Setup public status
        ObjectNode info = mapper.createObjectNode();
        info.put("isPublic", true);
        customer.setAdditionalInfo(info);

        ShortCustomerInfo shortInfo = customer.toShortCustomerInfo();

        assertThat(shortInfo).isNotNull();
        assertThat(shortInfo.getCustomerId()).isEqualTo(id);
        assertThat(shortInfo.getTitle()).isEqualTo("Big Corp");
        assertThat(shortInfo.isPublic()).isTrue();
    }

    // =====================================================================
    // 3. Fiabilité - Récupérabilité : robustesse face aux valeurs limites
    // =====================================================================

    @Test
    void createCustomer_withNullTitle_shouldAcceptNull() {
        Customer customer = new Customer();
        customer.setTitle(null);
        assertThat(customer.getTitle()).isNull();
        assertThat(customer.getName()).isNull();
    }

    @Test
    void createCustomer_defaultValues_shouldBeNull() {
        Customer customer = new Customer();
        assertThat(customer.getTitle()).isNull();
        assertThat(customer.getTenantId()).isNull();
        assertThat(customer.getId()).isNull();
        assertThat(customer.getEmail()).isNull();
        assertThat(customer.getPhone()).isNull();
        assertThat(customer.getCountry()).isNull();
    }

    // =====================================================================
    // 4. Maintenabilité - Copie, Update, Sérialisation
    // =====================================================================

    @Test
    void copyConstructor_shouldCreateIndependentCopy_AllFields() {
        Customer original = new Customer();
        original.setTitle("Original Corp");
        TenantId tenantId = new TenantId(UUID.randomUUID());
        CustomerId externalId = new CustomerId(UUID.randomUUID());
        original.setTenantId(tenantId);
        original.setEmail("contact@original.com");
        original.setCountry("Canada");
        original.setExternalId(externalId);
        original.setVersion(5L);

        Customer copy = new Customer(original);

        // Vérification de la copie des valeurs
        assertThat(copy.getTitle()).isEqualTo("Original Corp");
        assertThat(copy.getTenantId()).isEqualTo(tenantId);
        assertThat(copy.getEmail()).isEqualTo("contact@original.com");
        assertThat(copy.getCountry()).isEqualTo("Canada");
        assertThat(copy.getExternalId()).isEqualTo(externalId);
        assertThat(copy.getVersion()).isEqualTo(5L);

        // Vérification de l'indépendance
        copy.setTitle("Modified Corp");
        assertThat(original.getTitle()).isEqualTo("Original Corp");
        assertThat(copy.getTitle()).isEqualTo("Modified Corp");
    }

    @Test
    void testExternalIdAndVersion_Accessors() {
        Customer customer = new Customer();
        CustomerId extId = new CustomerId(UUID.randomUUID());

        customer.setExternalId(extId);
        customer.setVersion(10L);

        assertThat(customer.getExternalId()).isEqualTo(extId);
        assertThat(customer.getVersion()).isEqualTo(10L);
    }

    @Test
    void customerSerialization_shouldProduceValidJson() throws Exception {
        Customer customer = new Customer();
        customer.setTitle("Serialization Test");
        customer.setEmail("test@example.com");
        customer.setCountry("Canada");

        String json = mapper.writeValueAsString(customer);
        assertThat(json).contains("Serialization Test");
        assertThat(json).contains("test@example.com");

        Customer deserialized = mapper.readValue(json, Customer.class);
        assertThat(deserialized.getTitle()).isEqualTo("Serialization Test");
        assertThat(deserialized.getEmail()).isEqualTo("test@example.com");
        assertThat(deserialized.getCountry()).isEqualTo("Canada");
    }

    // =====================================================================
    // 5. Méthodes Générées et Manuelles (ToString, Equals, HashCode)
    // =====================================================================

    @Test
    void testEqualsAndHashCode() {
        CustomerId id = new CustomerId(UUID.randomUUID());
        Customer c1 = new Customer();
        c1.setId(id);
        c1.setTitle("Test");
        c1.setVersion(1L);

        Customer c2 = new Customer();
        c2.setId(id);
        c2.setTitle("Test");
        c2.setVersion(1L);

        Customer c3 = new Customer();
        c3.setTitle("Diff");

        // Test d'égalité
        assertThat(c1).isEqualTo(c1); // Réflexivité
        assertThat(c1).isEqualTo(c2); // Symétrie
        assertThat(c2).isEqualTo(c1);
        assertThat(c1).isNotEqualTo(c3); // Différence
        assertThat(c1).isNotEqualTo(null);
        assertThat(c1).isNotEqualTo(new Object());

        // Test de HashCode
        assertThat(c1.hashCode()).isEqualTo(c2.hashCode());
        assertThat(c1.hashCode()).isNotEqualTo(c3.hashCode());
    }

    @Test
    void testToString() {
        // Customer utilise un StringBuilder manuel, il faut vérifier qu'il ne crashe pas
        // et qu'il contient les champs essentiels
        Customer c = new Customer();
        c.setTitle("StringTest");
        c.setEmail("email@test.com");
        c.setCountry("CA");

        String str = c.toString();

        assertThat(str).isNotNull();
        assertThat(str).startsWith("Customer [");
        assertThat(str).endsWith("]");
        assertThat(str).contains("title=StringTest");
        assertThat(str).contains("email=email@test.com");
        assertThat(str).contains("country=CA");
    }
}