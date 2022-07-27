package top.yifan;

import top.yifan.entity.BaseObject;

/**
 * KafkaPartitionOffsetDTO
 *
 * @author Star Zheng
 */
public class KafkaPartitionOffsetDTO extends BaseObject {

    private Integer partition;

    private Long beginning;

    private Long end;

    private Long offset;

    private Long lag;

    public KafkaPartitionOffsetDTO() {}

    public KafkaPartitionOffsetDTO(Integer partition) {
        this.partition = partition;
    }

    public Integer getPartition() {
        return partition;
    }

    public void setPartition(Integer partition) {
        this.partition = partition;
    }

    public Long getBeginning() {
        return beginning;
    }

    public void setBeginning(Long beginning) {
        this.beginning = beginning;
    }

    public Long getEnd() {
        return end;
    }

    public void setEnd(Long end) {
        this.end = end;
    }

    public Long getOffset() {
        return offset;
    }

    public void setOffset(Long offset) {
        this.offset = offset;
    }

    public Long getLag() {
        return lag;
    }

    public void setLag(Long lag) {
        this.lag = lag;
    }
}
