package com.song.heracles.client.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author song
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageId {

  private long logSegmentSequenceNo;
  private long entryId;
  private long slotId;


}
