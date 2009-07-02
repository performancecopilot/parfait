/**
 * 
 */
package com.custardsource.parfait.dxm;

import com.custardsource.parfait.dxm.types.TypeHandler;

final class PcpMetricInfo implements PcpId, PcpOffset {
        private final String metricName;
        private final int id;
        
        private InstanceDomain domain;
        private TypeHandler<?> typeHandler;
        private int offset;
        private PcpString shortHelpText;
        private PcpString longHelpText;
        

        PcpMetricInfo(String metricName, int id) {
            this.metricName = metricName;
            this.id = id;
        }

        public int getId() {
            return id;
        }
        
        @Override
        public int getOffset() {
            return offset;
        }

        @Override
        public void setOffset(int offset) {
            this.offset = offset;
        }
        
        String getMetricName() {
            return metricName;
        }

        TypeHandler<?> getTypeHandler() {
            return typeHandler;
        }
        
        void setTypeHandler(TypeHandler<?> typeHandler) {
            if (this.typeHandler == null || this.typeHandler.equals(typeHandler)) {
                this.typeHandler = typeHandler;
            } else {
                throw new IllegalArgumentException(
                        "Two different type handlers cannot be registered for metric " + metricName);
            }
            
        }

        InstanceDomain getInstanceDomain() {
            return domain;
        }

        void setInstanceDomain(InstanceDomain domain) {
            if (this.domain == null || this.domain.equals(domain)) {
                this.domain = domain;
            } else {
                throw new IllegalArgumentException(
                        "Two different instance domains cannot be set for metric " + metricName);
            }
        }

        PcpString getShortHelpText() {
            return shortHelpText;
        }
        
        PcpString getLongHelpText() {
            return longHelpText;
        }

        void setHelpText(PcpString shortHelpText, PcpString longHelpText) {
            this.shortHelpText = shortHelpText;
            this.longHelpText = longHelpText;
        }
}