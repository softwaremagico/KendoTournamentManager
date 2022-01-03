import {Component, ViewChild, OnInit} from '@angular/core';
import {MatPaginator} from '@angular/material/paginator';
import {MatTableDataSource} from '@angular/material/table';
import {ClubService} from '../services/club.service';
import {Club} from '../models/club';
import {SelectionModel} from "@angular/cdk/collections";

@Component({
  selector: 'app-club-list',
  templateUrl: './club-list.component.html',
  styleUrls: ['./club-list.component.css']
})
export class ClubListComponent implements OnInit {

  columns: string[] = ['id', 'name', 'country', 'city', 'address', 'email', 'phone', 'web'];
  selection = new SelectionModel<Club>(false, []);
  clubs: Club[];
  dataSource: MatTableDataSource<Club>;

  @ViewChild(MatPaginator, {static: true}) paginator!: MatPaginator;

  constructor(private clubService: ClubService) {
  }

  ngOnInit(): void {
    this.showAllClubs();
    this.dataSource = new MatTableDataSource<Club>(this.clubs);
    this.dataSource.paginator = this.paginator;
  }

  showAllClubs(): void {
    // this.clubService.getAll()
    //   .subscribe(clubs => this.clubs = clubs);
    this.clubs = [
      {id: 1, name: 'Club1', country: 'Spain', city: 'Alcira'},
      {id: 2, name: 'Club2', country: 'Spain', city: 'Torremolinos'}
    ];
  }

  addClub(): void {

  }

  editClub(): void {

  }

  deleteClub(): void {

  }

}
