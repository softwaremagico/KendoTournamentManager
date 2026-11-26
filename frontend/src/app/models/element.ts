import {DatabaseObject} from "./database-object";

/**
 * Base class for all Angular domain model objects that map to backend JPA entities.
 *
 * Provides common audit fields ({@link createdAt}, {@link createdBy},
 * {@link updatedAt}, {@link updatedBy}) that mirror those of the backend
 * {@code Element} entity. On construction, {@link createdAt} is set to the
 * current timestamp and {@link createdBy} is populated from {@code localStorage}
 * with the logged-in username.
 */
export class Element extends DatabaseObject {
  /** Timestamp at which this record was first created. */
  public createdAt: Date;
  /** Timestamp of the last modification, or {@code undefined} for new records. */
  public updatedAt?: Date;
  /** Username of the user who created this record. */
  public createdBy?: string;
  /** Username of the user who last modified this record. */
  public updatedBy?: string;

  constructor() {
    super();
    this.createdAt = new Date();
    this.createdBy = localStorage.getItem('username')!;
  }


  public static override copy(source: Element, target: Element): void {
    super.copy(source, target);
    target.createdAt = source.createdAt;
    target.updatedAt = source.updatedAt;
    target.createdBy = source.createdBy;
    target.updatedBy = source.updatedBy;
  }

}
